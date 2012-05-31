package org.slf4j;

import android.util.Log;

public class LoggerImpl implements Logger {
	private String TAG = "Logger";

	public LoggerImpl(String name) {
		this.TAG = name;
	}

	@Override
	public String getName() {
		return TAG;
	}

	@Override
	public boolean isTraceEnabled() {
		return false;
	}

	@Override
	public void trace(String msg) {
		Log.v(TAG, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String format, Object[] argArray) {
		// TODO Auto-generated method stub

	}

	@Override
	public void trace(String msg, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDebugEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void debug(String msg) {
		Log.d(TAG, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void debug(String format, Object[] argArray) {
		// TODO Auto-generated method stub

	}

	@Override
	public void debug(String msg, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInfoEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void info(String msg) {
		Log.i(TAG, msg);
	}

	@Override
	public void info(String format, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void info(String format, Object[] argArray) {
		// TODO Auto-generated method stub

	}

	@Override
	public void info(String msg, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWarnEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void warn(String msg) {
		Log.w(TAG, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String format, Object[] argArray) {
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void warn(String msg, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isErrorEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void error(String msg) {
		Log.e(TAG, msg);
	}

	@Override
	public void error(String format, Object arg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String format, Object[] argArray) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String msg, Throwable t) {
		// TODO Auto-generated method stub

	}

}
