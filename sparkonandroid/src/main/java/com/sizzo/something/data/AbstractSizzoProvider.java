/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.sizzo.something.data;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteTransactionListener;
import android.net.Uri;

import java.util.ArrayList;

/**
 * A common base class for the contacts and profile providers.  This handles much of the same
 * logic that SQLiteContentProvider does (i.e. starting transactions on the appropriate database),
 * but exposes awareness of batch operations to the subclass so that cross-database operations
 * can be supported.
 */
public abstract class AbstractSizzoProvider extends ContentProvider
        implements SQLiteTransactionListener {

    /**
     * Duration in ms to sleep after successfully yielding the lock during a batch operation.
     */
    protected static final int SLEEP_AFTER_YIELD_DELAY = 4000;

    /**
     * Maximum number of operations allowed in a batch between yield points.
     */
    private static final int MAX_OPERATIONS_PER_YIELD_POINT = 500;

    /**
     * Number of inserts performed in bulk to allow before yielding the transaction.
     */
    private static final int BULK_INSERTS_PER_YIELD_POINT = 50;

    /**
     * The contacts transaction that is active in this thread.
     */
    private ThreadLocal<SizzoTransaction> mTransactionHolder;

    /**
     * The DB helper to use for this content provider.
     */
    private SQLiteOpenHelper mDbHelper;

    /**
     * The database helper to serialize all transactions on.  If non-null, any new transaction
     * created by this provider will automatically retrieve a writable database from this helper
     * and initiate a transaction on that database.  This should be used to ensure that operations
     * across multiple databases are all blocked on a single DB lock (to prevent deadlock cases).
     */
    private SQLiteOpenHelper mSerializeOnDbHelper;

    /**
     * The tag corresponding to the database used for serializing transactions.
     */
    private String mSerializeDbTag;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mDbHelper = getDatabaseHelper(context);
        mTransactionHolder = getTransactionHolder();
        return true;
    }

    public SQLiteOpenHelper getDatabaseHelper() {
        return mDbHelper;
    }

    /**
     * Specifies a database helper (and corresponding tag) to serialize all transactions on.
     * @param serializeOnDbHelper The database helper to use for serializing transactions.
     * @param tag The tag for this database.
     */
    public void setDbHelperToSerializeOn(SQLiteOpenHelper serializeOnDbHelper, String tag) {
        mSerializeOnDbHelper = serializeOnDbHelper;
        mSerializeDbTag = tag;
    }

    public SizzoTransaction getCurrentTransaction() {
        return mTransactionHolder.get();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SizzoTransaction transaction = startTransaction(false);
        try {
            Uri result = insertInTransaction(uri, values);
            if (result != null) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return result;
        } finally {
            endTransaction(false);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SizzoTransaction transaction = startTransaction(false);
        try {
            int deleted = deleteInTransaction(uri, selection, selectionArgs);
            if (deleted > 0) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return deleted;
        } finally {
            endTransaction(false);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SizzoTransaction transaction = startTransaction(false);
        try {
            int updated = updateInTransaction(uri, values, selection, selectionArgs);
            if (updated > 0) {
                transaction.markDirty();
            }
            transaction.markSuccessful(false);
            return updated;
        } finally {
            endTransaction(false);
        }
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SizzoTransaction transaction = startTransaction(true);
        int numValues = values.length;
        int opCount = 0;
        try {
            for (int i = 0; i < numValues; i++) {
                insert(uri, values[i]);
                if (++opCount >= BULK_INSERTS_PER_YIELD_POINT) {
                    opCount = 0;
                    try {
                        yield(transaction);
                    } catch (RuntimeException re) {
                        transaction.markYieldFailed();
                        throw re;
                    }
                }
            }
            transaction.markSuccessful(true);
        } finally {
            endTransaction(true);
        }
        return numValues;
    }

    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        int ypCount = 0;
        int opCount = 0;
        SizzoTransaction transaction = startTransaction(true);
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                if (++opCount >= MAX_OPERATIONS_PER_YIELD_POINT) {
                    throw new OperationApplicationException(
                            "Too many content provider operations between yield points. "
                                    + "The maximum number of operations per yield point is "
                                    + MAX_OPERATIONS_PER_YIELD_POINT, ypCount);
                }
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    opCount = 0;
                    try {
                        if (yield(transaction)) {
                            ypCount++;
                        }
                    } catch (RuntimeException re) {
                        transaction.markYieldFailed();
                        throw re;
                    }
                }

                results[i] = operation.apply(this, results, i);
            }
            transaction.markSuccessful(true);
            return results;
        } finally {
            endTransaction(true);
        }
    }

    /**
     * If we are not yet already in a transaction, this starts one (on the DB to serialize on, if
     * present) and sets the thread-local transaction variable for tracking.  If we are already in
     * a transaction, this returns that transaction, and the batch parameter is ignored.
     * @param callerIsBatch Whether the caller is operating in batch mode.
     */
    private SizzoTransaction startTransaction(boolean callerIsBatch) {
        SizzoTransaction transaction = mTransactionHolder.get();
        if (transaction == null) {
            transaction = new SizzoTransaction(callerIsBatch);
            if (mSerializeOnDbHelper != null) {
                transaction.startTransactionForDb(mSerializeOnDbHelper.getWritableDatabase(),
                        mSerializeDbTag, this);
            }
            mTransactionHolder.set(transaction);
        }
        return transaction;
    }

    /**
     * Ends the current transaction and clears out the member variable.  This does not set the
     * transaction as being successful.
     * @param callerIsBatch Whether the caller is operating in batch mode.
     */
    private void endTransaction(boolean callerIsBatch) {
        SizzoTransaction transaction = mTransactionHolder.get();
        if (transaction != null && (!transaction.isBatch() || callerIsBatch)) {
            try {
                if (transaction.isDirty()) {
                    notifyChange();
                }
                transaction.finish(callerIsBatch);
            } finally {
                // No matter what, make sure we clear out the thread-local transaction reference.
                mTransactionHolder.set(null);
            }
        }
    }

    /**
     * Gets the database helper for this contacts provider.  This is called once, during onCreate().
     */
    protected abstract SQLiteOpenHelper getDatabaseHelper(Context context);

    /**
     * Gets the thread-local transaction holder to use for keeping track of the transaction.  This
     * is called once, in onCreate().  If multiple classes are inheriting from this class that need
     * to be kept in sync on the same transaction, they must all return the same thread-local.
     */
    protected abstract ThreadLocal<SizzoTransaction> getTransactionHolder();

    protected abstract Uri insertInTransaction(Uri uri, ContentValues values);

    protected abstract int deleteInTransaction(Uri uri, String selection, String[] selectionArgs);

    protected abstract int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs);

    protected abstract boolean yield(SizzoTransaction transaction);

    protected abstract void notifyChange();
}
