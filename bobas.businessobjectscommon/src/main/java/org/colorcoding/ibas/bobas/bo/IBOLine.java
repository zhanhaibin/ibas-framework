package org.colorcoding.ibas.bobas.bo;

/**
 * 行对象
 * 
 * @author Niuren.Zhu
 *
 */
public interface IBOLine extends IBusinessObject {

	/**
	 * 获取-行编号 主键
	 * 
	 * @return
	 */
	Integer getLineId();

	/**
	 * 设置-行编号 主键
	 * 
	 * @param value
	 */
	void setLineId(Integer value);
}
