package org.colorcoding.ibas.bobas.data;

import javax.xml.bind.annotation.XmlType;

import org.colorcoding.ibas.bobas.MyConsts;
import org.colorcoding.ibas.bobas.mapping.Value;

/**
 * 条件-关系
 * 
 * @author Niuren.Zhu
 *
 */
@XmlType(name = "emConditionRelationship", namespace = MyConsts.NAMESPACE_BOBAS_DATA)
public enum emConditionRelationship {
	/**
	 * 无
	 */
	@Value("NON")
	NONE,
	/**
	 * 并且
	 */
	@Value("AND")
	AND,
	/**
	 * 或者
	 */
	@Value("OR")
	OR;

	public int getValue() {
		return this.ordinal();
	}

	public static emConditionRelationship forValue(int value) {
		return values()[value];
	}
}
