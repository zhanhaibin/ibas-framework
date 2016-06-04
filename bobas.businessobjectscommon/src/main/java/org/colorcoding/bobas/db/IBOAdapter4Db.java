package org.colorcoding.bobas.db;

import org.colorcoding.bobas.common.ICriteria;
import org.colorcoding.bobas.common.ISqlQuery;
import org.colorcoding.bobas.core.IBusinessObjectBase;
import org.colorcoding.bobas.core.IBusinessObjectListBase;
import org.colorcoding.bobas.data.KeyValue;
import org.colorcoding.bobas.repository.TransactionType;

/**
 * 业务对象的数据库适配器
 */
public interface IBOAdapter4Db {
	/**
	 * 解析查询
	 * 
	 * @param criteria
	 *            查询
	 * @return 查询对象
	 * @throws SqlScriptsException
	 */
	ISqlQuery parseSqlQuery(ICriteria criteria) throws BOParseException;

	/**
	 * 解析查询
	 * 
	 * @param criteria
	 *            查询
	 * @param boType
	 *            对象类型
	 * @return 查询脚本
	 */
	ISqlQuery parseSqlQuery(ICriteria criteria, Class<?> boType) throws BOParseException;

	/**
	 * 获取服务器时间脚本
	 * 
	 * @return 脚本
	 * @throws BOParseException
	 */
	ISqlQuery getServerTimeScript() throws BOParseException;

	/**
	 * 获取插入语句
	 * 
	 * @param bo
	 *            业务对象
	 * @return 插入语句
	 * @throws SqlScriptsException
	 */
	ISqlQuery parseSqlInsert(IBusinessObjectBase bo) throws BOParseException;

	/**
	 * 获取删除语句
	 * 
	 * @param bo
	 *            业务对象
	 * @return 删除语句
	 * @throws SqlScriptsException
	 */
	ISqlQuery parseSqlDelete(IBusinessObjectBase bo) throws BOParseException;

	/**
	 * 获取更新语句
	 * 
	 * @param bo
	 *            业务对象
	 * @return 更新语句
	 * @throws SqlScriptsException
	 */
	ISqlQuery parseSqlUpdate(IBusinessObjectBase bo) throws BOParseException;

	/**
	 * 解析业务对象
	 * 
	 * @param reader
	 *            数据
	 * @param boType
	 *            业务对象
	 * 
	 * @return 解析的对象数组
	 * @throws BOParseException
	 */
	IBusinessObjectBase[] parseBOs(IDbDataReader reader, Class<?> boType) throws BOParseException;

	/**
	 * 解析业务对象
	 * 
	 * @param reader
	 *            数据
	 * @param bo
	 *            业务对象实例
	 * 
	 * @return 解析的对象
	 * @throws BOParseException
	 */
	IBusinessObjectBase[] parseBOs(IDbDataReader reader, IBusinessObjectListBase<?> bos) throws BOParseException;

	/**
	 * 解析业务对象
	 * 
	 * @param bo
	 *            业务对象
	 * @param command
	 *            数据库命令
	 * 
	 * @return 主键及值
	 * @throws BOParseException
	 */
	KeyValue[] parsePrimaryKeys(IBusinessObjectBase bo, IDbCommand command) throws BOParseException;

	/**
	 * 更新业务对象主键记录
	 * 
	 * @param bo
	 *            业务对象（取值对象）
	 * @param command
	 *            数据库命令
	 * 
	 * @throws BOParseException
	 */
	void updatePrimaryKeyRecords(IBusinessObjectBase bo, IDbCommand command) throws BOParseException;

	/**
	 * 使用主键（查询并更新） 应考虑多线程的并发问题
	 * 
	 * @param bo
	 *            业务对象
	 * @param command
	 *            数据库命令
	 * @return 业务对象主键
	 * @throws BOParseException
	 */
	KeyValue[] usePrimaryKeys(IBusinessObjectBase bo, IDbCommand command) throws BOParseException;

	/**
	 * 获取事务通知语句
	 * 
	 * @param bo
	 *            业务对象
	 * @return 通知语句
	 * @throws BOParseException
	 */
	ISqlQuery parseBOTransactionNotification(TransactionType type, IBusinessObjectBase bo) throws BOParseException;
}