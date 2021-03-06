package org.colorcoding.ibas.bobas.test.logics;

import org.colorcoding.ibas.bobas.data.Decimal;
import org.colorcoding.ibas.bobas.logics.IBusinessLogicContract;

/**
 * 物料库存数量逻辑契约
 * 
 * @author Niuren.Zhu
 *
 */
public interface IMaterialsInventoryQuantityContract extends IBusinessLogicContract {
	/**
	 * 物料编码
	 * 
	 * @return
	 */
	String getItemCode();

	/**
	 * 数量
	 * 
	 * @return
	 */
	Decimal getQuantity();
}
