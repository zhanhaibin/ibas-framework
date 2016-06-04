package org.colorcoding.bobas.repository;

import java.lang.reflect.Array;

import org.colorcoding.bobas.common.IOperationResult;
import org.colorcoding.bobas.common.ISqlQuery;
import org.colorcoding.bobas.common.OperationResult;
import org.colorcoding.bobas.core.IBusinessObjectBase;
import org.colorcoding.bobas.core.IBusinessObjectListBase;
import org.colorcoding.bobas.core.ITrackStatusOperator;
import org.colorcoding.bobas.core.RepositoryException;
import org.colorcoding.bobas.core.SaveActionsException;
import org.colorcoding.bobas.core.SaveActionsListener;
import org.colorcoding.bobas.core.SaveActionsSupport;
import org.colorcoding.bobas.core.SaveActionsType;
import org.colorcoding.bobas.core.fields.IFieldData;
import org.colorcoding.bobas.core.fields.IManageFields;
import org.colorcoding.bobas.db.DbException;
import org.colorcoding.bobas.db.IBOAdapter4Db;
import org.colorcoding.bobas.db.IDbCommand;
import org.colorcoding.bobas.db.IDbDataReader;
import org.colorcoding.bobas.i18n.i18n;
import org.colorcoding.bobas.messages.RuntimeLog;

/**
 * 基本数据库仓库-读写
 */
class BORepository4Db extends BORepository4DbReadonly implements IBORepository4Db {

	public BORepository4Db() {
	}

	public BORepository4Db(String sign) {
		super(sign);
	}

	@Override
	public boolean beginTransaction() throws RepositoryException {
		try {
			boolean done = this.getDbConnection().beginTransaction();
			this.setTransactionId();
			return done;
		} catch (DbException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void rollbackTransaction() throws RepositoryException {
		try {
			this.getDbConnection().rollbackTransaction();
			this.setTransactionId(null);
		} catch (DbException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public void commitTransaction() throws RepositoryException {
		try {
			this.getDbConnection().commitTransaction();
			this.setTransactionId(null);
		} catch (DbException e) {
			throw new RepositoryException(e);
		}
	}

	@Override
	public boolean inTransaction() {
		try {
			return this.getDbConnection().inTransaction();
		} catch (DbException e) {
			return false;
		}
	}

	private volatile SaveActionsSupport saveActionsSupport;

	/**
	 * 通知事务
	 * 
	 * @param type
	 *            事务类型
	 * @param bo
	 *            发生业务对象
	 * @return 是否继续执行
	 * @throws SaveActionsException
	 *             运行时错误
	 */
	protected final boolean notifyActions(SaveActionsType type, IBusinessObjectBase bo) throws SaveActionsException {
		if (this.saveActionsSupport == null) {
			return true;
		}
		return this.saveActionsSupport.fireActions(type, bo);
	}

	/**
	 * 添加事务监听
	 * 
	 * @param listener
	 */
	@Override
	public final void addSaveActionsListener(SaveActionsListener listener) {
		if (this.saveActionsSupport == null) {
			this.saveActionsSupport = new SaveActionsSupport(this);
		}
		this.saveActionsSupport.addListener(listener);
	}

	/**
	 * 移出事务监听
	 * 
	 * @param listener
	 */
	@Override
	public final void removeSaveActionsListener(SaveActionsListener listener) {
		if (this.saveActionsSupport == null) {
			return;
		}
		this.saveActionsSupport.removeListener(listener);
	}

	@Override
	public IOperationResult<?> save(IBusinessObjectBase bo) {
		OperationResult<?> operationResult = new OperationResult<Object>();
		try {
			IBusinessObjectBase nBO = this.mySave(bo);
			if (nBO instanceof ITrackStatusOperator) {
				// 保存成功，标记对象为OLD
				ITrackStatusOperator operator = (ITrackStatusOperator) nBO;
				operator.markOld(true);
			}
			operationResult.addResultObjects(nBO);
		} catch (Exception e) {
			operationResult.setError(e);
			RuntimeLog.log(e);
		}
		return operationResult;
	}

	@Override
	public IOperationResult<?> saveEx(IBusinessObjectBase bo) {
		OperationResult<?> operationResult = new OperationResult<Object>();
		try {
			IBusinessObjectBase nBO = this.mySaveEx(bo);
			if (nBO instanceof ITrackStatusOperator) {
				// 保存成功，标记对象为OLD
				ITrackStatusOperator operator = (ITrackStatusOperator) nBO;
				operator.markOld(true);
			}
			operationResult.addResultObjects(nBO);
		} catch (Exception e) {
			operationResult.setError(e);
			RuntimeLog.log(e);
		}
		return operationResult;
	}

	/**
	 * 保存对象，不包括子属性
	 * 
	 * @param bo
	 *            对象
	 * @return 保存的对象
	 * @throws Exception
	 */
	private final IBusinessObjectBase mySave(IBusinessObjectBase bo) throws Exception {
		if (bo == null) {
			throw new RepositoryException(i18n.prop("msg_bobas_invalid_bo"));
		}
		if (bo.isDirty()) {
			// 仅修过的数据进行处理
			IBOAdapter4Db adapter4Db = this.createDbAdapter().createBOAdapter();
			IDbDataReader reader = null;
			IDbCommand command = null;
			boolean myOpenedDb = false;// 自己打开的数据库
			boolean myTrans = false;// 自己打开的事务
			try {
				ISqlQuery sqlQuery = null;
				// 开始保存数据
				myOpenedDb = this.openDbConnection();
				myTrans = this.beginTransaction();
				command = this.getDbConnection().createCommand();
				this.tagStorage(bo);// 存储标记
				if (bo.isNew()) {
					// 新建的对象
					adapter4Db.usePrimaryKeys(bo, command);// 获取并更新主键
					this.notifyActions(SaveActionsType.before_adding, bo);
					sqlQuery = adapter4Db.parseSqlInsert(bo);
				} else if (bo.isDeleted()) {
					// 删除对象
					this.notifyActions(SaveActionsType.before_deleting, bo);
					sqlQuery = adapter4Db.parseSqlDelete(bo);
				} else {
					// 修改对象，先删除数据，再添加新的实例
					this.notifyActions(SaveActionsType.before_updating, bo);
					sqlQuery = adapter4Db.parseSqlDelete(bo);
					command.executeUpdate(sqlQuery);// 执行删除副本
					sqlQuery = adapter4Db.parseSqlInsert(bo);
				}
				// 运行保存语句
				command.executeUpdate(sqlQuery);
				// 通知事务
				if (bo.isNew()) {
					// 新建的对象
					this.notifyActions(SaveActionsType.added, bo);
				} else if (bo.isDeleted()) {
					// 删除对象
					this.notifyActions(SaveActionsType.deleted, bo);
				} else {
					// 修改对象
					this.notifyActions(SaveActionsType.updated, bo);
				}
				if (myTrans) {
					// 自己打开的事务
					this.commitTransaction();// 关闭事务
				}
			} catch (Exception e) {
				if (myTrans) {
					// 自己打开的事务
					this.rollbackTransaction();// 关闭事务
				}
				throw new DbException(i18n.prop("msg_bobas_save_bo_faild", e.getMessage()), e);
			} finally {
				if (reader != null) {
					reader.close();
				}
				if (command != null) {
					command.close();
				}
				if (myOpenedDb) {
					// 自己开打自己关闭
					this.closeDbConnection();// 关闭数据库连接
				}
			}
		}
		return bo;
	}

	/**
	 * 保存对象，包括子属性
	 * 
	 * @param bo
	 *            对象
	 * @return 保存的对象
	 * @throws Exception
	 */
	private final IBusinessObjectBase mySaveEx(IBusinessObjectBase bo) throws Exception {
		if (bo == null) {
			throw new RepositoryException(i18n.prop("msg_bobas_invalid_bo"));
		}
		if (bo.isDirty()) {
			// 仅修过的数据进行处理
			boolean myOpenedDb = false;// 自己打开的数据库
			boolean myTrans = false;// 自己打开的事务
			try {
				// 开始保存数据
				myOpenedDb = this.openDbConnection();
				myTrans = this.beginTransaction();
				// 保存主对象
				this.mySave(bo);
				// 保存子项
				if (bo instanceof IManageFields) {
					IManageFields boFields = (IManageFields) bo;
					for (IFieldData fieldData : boFields.getFields()) {
						if (!fieldData.isSavable()) {
							// 不保存字段，继续下一个
							continue;
						}
						Object fdValue = fieldData.getValue();
						if (fdValue == null) {
							// 空值，继续下一个
							continue;
						}
						if (fdValue instanceof IBusinessObjectListBase<?>) {
							// 对象列表
							IBusinessObjectListBase<?> childs = (IBusinessObjectListBase<?>) fdValue;
							for (IBusinessObjectBase childBO : childs) {
								this.mySaveEx(childBO);// 继续带子项的保存
							}
						} else if (fdValue instanceof IBusinessObjectBase) {
							// 对象属性
							this.mySaveEx((IBusinessObjectBase) fdValue);// 继续带子项的保存
						} else if (fdValue.getClass().isArray()) {
							// 对象数组
							int length = Array.getLength(fdValue);
							for (int i = 0; i < length; i++) {
								Object child = Array.get(fdValue, i);
								if (child instanceof IBusinessObjectBase) {
									IBusinessObjectBase childBO = (IBusinessObjectBase) child;
									this.mySaveEx(childBO);// 继续带子项的保存
								}
							}
						}
					}
				}
				if (myTrans) {
					// 自己打开的事务
					this.commitTransaction();// 关闭事务
				}
			} catch (Exception e) {
				if (myTrans) {
					// 自己打开的事务
					this.rollbackTransaction();// 关闭事务
				}
				throw e;
			} finally {
				if (myOpenedDb) {
					// 自己开打自己关闭
					this.closeDbConnection();// 关闭数据库连接
				}
			}
		}
		return bo;
	}

}