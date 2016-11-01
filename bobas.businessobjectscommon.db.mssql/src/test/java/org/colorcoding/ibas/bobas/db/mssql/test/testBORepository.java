package org.colorcoding.ibas.bobas.db.mssql.test;

import org.colorcoding.ibas.bobas.bo.IBOUserFields;
import org.colorcoding.ibas.bobas.bo.IUserField;
import org.colorcoding.ibas.bobas.common.ConditionRelationship;
import org.colorcoding.ibas.bobas.common.Criteria;
import org.colorcoding.ibas.bobas.common.IChildCriteria;
import org.colorcoding.ibas.bobas.common.ICondition;
import org.colorcoding.ibas.bobas.common.ICriteria;
import org.colorcoding.ibas.bobas.common.IOperationResult;
import org.colorcoding.ibas.bobas.common.ISort;
import org.colorcoding.ibas.bobas.common.ISqlQuery;
import org.colorcoding.ibas.bobas.common.SortType;
import org.colorcoding.ibas.bobas.common.SqlQuery;
import org.colorcoding.ibas.bobas.core.RepositoryException;
import org.colorcoding.ibas.bobas.data.ComputeException;
import org.colorcoding.ibas.bobas.data.DateTime;
import org.colorcoding.ibas.bobas.data.IDataTable;
import org.colorcoding.ibas.bobas.data.emDocumentStatus;
import org.colorcoding.ibas.bobas.data.measurement.emTimeUnit;
import org.colorcoding.ibas.bobas.repository.BORepository4DbReadonly;
import org.colorcoding.ibas.bobas.repository.IBORepository4DbReadonly;
import org.colorcoding.ibas.bobas.repository.InvalidRepositoryException;
import org.colorcoding.ibas.bobas.test.bo.IMaterials;
import org.colorcoding.ibas.bobas.test.bo.ISalesOrder;
import org.colorcoding.ibas.bobas.test.bo.ISalesOrderItem;
import org.colorcoding.ibas.bobas.test.bo.IUser;
import org.colorcoding.ibas.bobas.test.bo.Materials;
import org.colorcoding.ibas.bobas.test.bo.SalesOrder;
import org.colorcoding.ibas.bobas.test.bo.SalesOrderItem;
import org.colorcoding.ibas.bobas.test.bo.User;
import org.colorcoding.ibas.bobas.test.logics.MaterialsQuantityJournal;
import org.colorcoding.ibas.bobas.test.logics.PurchaseOrder;
import org.colorcoding.ibas.bobas.test.logics.PurchaseOrderItem;
import org.colorcoding.ibas.bobas.test.repository.BORepositoryTest;

import junit.framework.TestCase;

public class testBORepository extends TestCase {

	public boolean details_out = true;

	public void testCriteria() throws InvalidRepositoryException {
		ICriteria criteria = new Criteria();
		criteria.setNotLoadedChildren(true);
		criteria.setResultCount(100);
		// ("DocStatus" = 'P' OR "DocStatus" = 'F')
		ICondition condition = criteria.getConditions().create();
		condition.setBracketOpenNum(1);
		condition.setAlias(SalesOrder.DocumentStatusProperty.getName());
		condition.setCondVal(emDocumentStatus.Planned);
		condition = criteria.getConditions().create();
		condition.setBracketCloseNum(1);
		condition.setAlias(SalesOrder.DocumentStatusProperty.getName());
		condition.setCondVal(emDocumentStatus.Released);
		condition.setRelationship(ConditionRelationship.cr_OR);
		// ORDER BY "DocEntry" DESC, "CardCode" ASC
		ISort sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.DocEntryProperty.getName());
		sort.setSortType(SortType.st_Descending);
		sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.CustomerCodeProperty.getName());
		sort.setSortType(SortType.st_Ascending);

		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUserToken("");
		IOperationResult<?> operationResult = boRepository.fetchSalesOrder(criteria);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));

		criteria.setNotLoadedChildren(false);
		// 查询子项
		IChildCriteria childCriteria = criteria.getChildCriterias().create();
		childCriteria.setPropertyPath(SalesOrder.SalesOrderItemsProperty.getName());
		// childCriteria.setFatherMustHasResluts(false);//父项必须有返回值
		condition = childCriteria.getConditions().create();
		condition.setAlias(SalesOrderItem.LineStatusProperty.getName());
		condition.setCondVal(emDocumentStatus.Finished);

		boRepository = new BORepositoryTest();// 重新构造后缓存失效
		boRepository.setUserToken("");
		operationResult = boRepository.fetchSalesOrder(criteria);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));

	}

	public void testConnectBORepository() {
		// System.out.println(System.getProperty("java.class.path"));
		// System.out.println(System.getProperty("user.dir"));
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUserToken("");
		DateTime dateTime = boRepository.getServerTime();
		System.out.println(dateTime.toString());
	}

	public void testBORepositoryTest() {
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUserToken("");
		ISalesOrder order = new SalesOrder();
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		ISalesOrderItem item = order.getSalesOrderItems().create();
		item.setItemCode("T800");
		item.setItemDescription("终结者机器人-T800");
		item.setQuantity(1);
		item.setPrice(999999.99);
		item = order.getSalesOrderItems().create();
		item.setItemCode("S001");
		item.setItemDescription("绝地武士-剑");
		item.setQuantity(2);
		item.setPrice(99.00);
		IOperationResult<ISalesOrder> operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

		ICriteria criteria = order.getCriteria();// new Criteria();
		criteria.setResultCount(1);
		ISort sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.DocEntryProperty.getName());
		sort.setSortType(SortType.st_Descending);

		operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

	}

	public void testFetchBO() throws InvalidRepositoryException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ICriteria criteria = new Criteria();
		criteria.setResultCount(100);
		// ("DocStatus" = 'P' OR "DocStatus" = 'F')
		ICondition condition = criteria.getConditions().create();
		condition.setBracketOpenNum(1);
		condition.setAlias(SalesOrder.DocumentStatusProperty.getName());
		condition.setCondVal(emDocumentStatus.Planned);
		condition = criteria.getConditions().create();
		condition.setBracketCloseNum(1);
		condition.setAlias(SalesOrder.DocumentStatusProperty.getName());
		condition.setCondVal(emDocumentStatus.Released);
		condition.setRelationship(ConditionRelationship.cr_OR);
		// ORDER BY "DocEntry" DESC, "CardCode" ASC
		ISort sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.DocEntryProperty.getName());
		sort.setSortType(SortType.st_Descending);
		sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.CustomerCodeProperty.getName());
		sort.setSortType(SortType.st_Ascending);

		IOperationResult<?> operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

		for (int i = 0; i < 3; i++) {
			operationResult = boRepository.fetchSalesOrder(criteria);
			System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
					operationResult.getMessage(), operationResult.getResultObjects().size()));
			assertEquals(operationResult.getResultCode(), 0);
		}

		if (details_out) {
			for (Object item : operationResult.getResultObjects()) {
				if (item instanceof IBOUserFields) {
					IBOUserFields userFields = (IBOUserFields) item;
					System.out.println(String.format("%s user fields count:%s", item.toString(),
							userFields.getUserFields().size()));
					for (IUserField field : userFields.getUserFields()) {
						System.out.println(
								String.format(" %s %s %s", field.getName(), field.getValue(), field.getValueType()));
					}
				}
				System.out.println(String.format("%s complex field value:%s", item.toString(),
						((ISalesOrder) item).getCycle().toString()));
			}

		}
	}

	public void testSaveBO() throws InvalidRepositoryException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ISalesOrder order = new SalesOrder();
		order.setDocumentUser(new User());
		order.getDocumentUser().setUserCode(DateTime.getNow().toString("HHmmss") + "00");
		order.setTeamUsers(new User[] { new User(), new User() });
		order.getTeamUsers()[0].setUserCode(DateTime.getNow().toString("HHmmss") + "01");
		order.getTeamUsers()[1].setUserCode(DateTime.getNow().toString("HHmmss") + "02");
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		ISalesOrderItem item = order.getSalesOrderItems().create();
		item.setItemCode("T800");
		item.setItemDescription("终结者机器人-T800");
		item.setQuantity(1);
		item.setPrice(999999.99);
		item = order.getSalesOrderItems().create();
		item.setItemCode("S001");
		item.setItemDescription("绝地武士-剑");
		item.setQuantity(2);
		item.setPrice(99.00);
		IOperationResult<?> operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		order.setCustomerName("宇宙无敌影业--");
		order.getSalesOrderItems().get(0).delete();
		item = order.getSalesOrderItems().get(1);
		item.setQuantity(20);
		item = order.getSalesOrderItems().create();
		item.setItemCode("S003");
		item.setItemDescription("绝地武士-头盔");
		item.setQuantity(3);
		item.setPrice(299.00);
		operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		order.delete();
		operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
	}

	public void testBOAssociations() throws InvalidRepositoryException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ICriteria criteria = new Criteria();

		IOperationResult<?> operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		ISalesOrder order = (ISalesOrder) operationResult.getResultObjects().firstOrDefault();
		System.out.println(order.toString("xml"));
		IUser documentUser = order.getDocumentUser();
		if (documentUser != null) {
			System.out.println(String.format("user:%s ", documentUser));
		}
		IUser[] teamUsers = order.getTeamUsers();
		if (teamUsers != null) {
			System.out.println(String.format("team user count:%s ", teamUsers.length));
		}
	}

	static boolean flagStop = false;

	public void testExtremeTask() {
		try {
			for (int i = 0; i < 10; i++) {
				Thread ts = new Thread() {
					@Override
					public void run() {
						testBORepository test = new testBORepository();
						test.details_out = false;
						while (!flagStop) {
							try {
								test.testSaveBO();
							} catch (InvalidRepositoryException e) {
								e.printStackTrace();
							}
						}
					}
				};
				ts.start();
				Thread tf = new Thread() {
					@Override
					public void run() {
						testBORepository test = new testBORepository();
						test.details_out = false;
						while (!flagStop) {
							try {
								test.testFetchBO();
							} catch (InvalidRepositoryException e) {
								e.printStackTrace();
							}
						}
					}
				};
				tf.start();
			}
			Thread.sleep(50000);
			flagStop = true;
			Thread.sleep(50000);// 继续等待，资源释放
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void testQuery() {
		IBORepository4DbReadonly boRepository = new BORepository4DbReadonly();
		ISqlQuery sqlQuery = new SqlQuery();
		sqlQuery.setQueryString("select * from cc_tt_ordr");

		IOperationResult<IDataTable> operationResult = boRepository.query(sqlQuery);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		System.out.println(operationResult.getResultObjects().firstOrDefault().toString("json"));

	}

	public void testMaterialsOrderQuantity() {
		// 创建物料数据
		IMaterials materials01 = new Materials();
		materials01.setItemCode(String.format("A%s", DateTime.getNow().getTime()));
		materials01.setItemDescription("CPU i7");
		IMaterials materials02 = new Materials();
		materials02.setItemCode(String.format("B%s", DateTime.getNow().getTime()));
		materials02.setItemDescription("Disk 5T");
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUseCache(false);// 缓存会导致数据检索不到
		IOperationResult<?> operationResult;

		operationResult = boRepository.saveMaterials(materials01);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		operationResult = boRepository.saveMaterials(materials02);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		// 创建采购订单
		PurchaseOrder order = new PurchaseOrder();
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		PurchaseOrderItem item01 = order.getPurchaseOrderItems().create();
		item01.setItemCode(materials01.getItemCode());
		item01.setItemDescription(materials01.getItemDescription());
		item01.setQuantity(1);
		item01.setPrice(999999.99);
		PurchaseOrderItem item02 = order.getPurchaseOrderItems().create();
		item02.setItemCode(materials02.getItemCode());
		item02.setItemDescription(materials02.getItemDescription());
		item02.setQuantity(2);
		item02.setPrice(99.00);
		PurchaseOrderItem item03 = order.getPurchaseOrderItems().create();
		item03.setItemCode(materials02.getItemCode());
		item03.setItemDescription(materials02.getItemDescription());
		item03.setQuantity(9);
		item03.setPrice(99.00);

		operationResult = boRepository.savePurchaseOrder(order);
		if (operationResult.getResultCode() != 0) {
			System.err.println(operationResult.getMessage());
		}
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		operationResult = boRepository.fetchMaterials(materials01.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		IMaterials materials01s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		// 检索的物料是否一致
		assertEquals("materials not same.", materials01.getItemCode(), materials01s.getItemCode());
		// 订购数量是否增加
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials01.getItemCode()),
				materials01.getOnOrder().add(item01.getQuantity()).floatValue(),
				materials01s.getOnOrder().floatValue());
		operationResult = boRepository.fetchMaterials(materials02.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		IMaterials materials02s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials02.getItemCode(), materials02s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials02.getItemCode()),
				materials02.getOnOrder().add(item02.getQuantity().add(item03.getQuantity())).floatValue(),
				materials02s.getOnOrder().floatValue());

		// 修改数量
		item01.setQuantity(20);
		operationResult = boRepository.savePurchaseOrder(order);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		operationResult = boRepository.fetchMaterials(materials02.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		materials02s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials02.getItemCode(), materials02s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials02.getItemCode()),
				materials02.getOnOrder().add(item02.getQuantity().add(item03.getQuantity())).floatValue(),
				materials02s.getOnOrder().floatValue());

		operationResult = boRepository.fetchMaterials(materials01.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		materials01s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials01.getItemCode()),
				materials01.getOnOrder().add(item01.getQuantity()).floatValue(),
				materials01s.getOnOrder().floatValue());

		// 删除数据
		item03.delete();
		operationResult = boRepository.savePurchaseOrder(order);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		operationResult = boRepository.fetchMaterials(materials02.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		materials02s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials02.getItemCode(), materials02s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials02.getItemCode()),
				materials02.getOnOrder().add(item02.getQuantity()).floatValue(),
				materials02s.getOnOrder().floatValue());

	}

	public void testMaterialsJournal() {
		// 创建物料数据
		IMaterials materials01 = new Materials();
		materials01.setItemCode(String.format("A%s", DateTime.getNow().getTime()));
		materials01.setItemDescription("CPU i7");
		IMaterials materials02 = new Materials();
		materials02.setItemCode(String.format("B%s", DateTime.getNow().getTime()));
		materials02.setItemDescription("Disk 5T");
		// 保存物料到文件系统
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUseCache(false);// 缓存会导致数据检索不到
		IOperationResult<?> operationResult;

		operationResult = boRepository.saveMaterials(materials01);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		operationResult = boRepository.saveMaterials(materials02);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		// 创建采购订单
		PurchaseOrder order = new PurchaseOrder();
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		PurchaseOrderItem item01 = order.getPurchaseOrderItems().create();
		item01.setItemCode(materials01.getItemCode());
		item01.setItemDescription(materials01.getItemDescription());
		item01.setQuantity(1);
		item01.setPrice(999999.99);
		PurchaseOrderItem item02 = order.getPurchaseOrderItems().create();
		item02.setItemCode(materials02.getItemCode());
		item02.setItemDescription(materials02.getItemDescription());
		item02.setQuantity(2);
		item02.setPrice(99.00);
		PurchaseOrderItem item03 = order.getPurchaseOrderItems().create();
		item03.setItemCode(materials02.getItemCode());
		item03.setItemDescription(materials02.getItemDescription());
		item03.setQuantity(9);
		item03.setPrice(99.00);

		operationResult = boRepository.savePurchaseOrder(order);
		if (operationResult.getResultCode() != 0) {
			System.err.println(operationResult.getMessage());
		}
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		operationResult = boRepository.fetchMaterials(materials01.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		IMaterials materials01s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials01.getItemCode(), materials01s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials01.getItemCode()),
				materials01.getOnOrder().add(item01.getQuantity()).floatValue(),
				materials01s.getOnOrder().floatValue());
		assertEquals(String.format("wrong matrials [%s] hand quantity.", materials01.getItemCode()),
				materials01.getOnHand().add(item01.getQuantity()).floatValue(), materials01s.getOnHand().floatValue());
		operationResult = boRepository.fetchMaterials(materials02.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		IMaterials materials02s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials02.getItemCode(), materials02s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials02.getItemCode()),
				materials02.getOnOrder().add(item02.getQuantity().add(item03.getQuantity())).floatValue(),
				materials02s.getOnOrder().floatValue());
		assertEquals(String.format("wrong matrials [%s] hand quantity.", materials02.getItemCode()),
				materials02.getOnHand().add(item02.getQuantity().add(item03.getQuantity())).floatValue(),
				materials02s.getOnHand().floatValue());

		ICriteria criteria = Criteria.create();
		ICondition condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentTypeProperty.getName());
		condition.setCondVal(item01.getDocumentType());
		condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentEntryProperty.getName());
		condition.setCondVal(item01.getDocumentEntry());
		condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentLineIdProperty.getName());
		condition.setCondVal(item01.getDocumentLineId());
		operationResult = boRepository.fetchMaterialsQuantityJournal(criteria);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		MaterialsQuantityJournal journal01 = (MaterialsQuantityJournal) operationResult.getResultObjects()
				.firstOrDefault();
		assertEquals("materials not same.", item01.getItemCode(), journal01.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", item01.getItemCode()),
				item01.getQuantity().floatValue(), journal01.getQuantity().floatValue());

		item01.setQuantity(20);
		item03.delete();
		operationResult = boRepository.savePurchaseOrder(order);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);

		operationResult = boRepository.fetchMaterials(materials02.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		materials02s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals("materials not same.", materials02.getItemCode(), materials02s.getItemCode());
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials02.getItemCode()),
				materials02.getOnOrder().add(item02.getQuantity()).floatValue(),
				materials02s.getOnOrder().floatValue());
		assertEquals(String.format("wrong matrials [%s] hand quantity.", materials02.getItemCode()),
				materials02.getOnHand().add(item02.getQuantity()).floatValue(), materials02s.getOnHand().floatValue());

		operationResult = boRepository.fetchMaterials(materials01.getCriteria());
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		materials01s = (IMaterials) operationResult.getResultObjects().firstOrDefault();
		assertEquals(String.format("wrong matrials [%s] order quantity.", materials01.getItemCode()),
				materials01.getOnOrder().add(item01.getQuantity()).floatValue(),
				materials01s.getOnOrder().floatValue());
		assertEquals(String.format("wrong matrials [%s] hand quantity.", materials01.getItemCode()),
				materials01.getOnHand().add(item01.getQuantity()).floatValue(), materials01s.getOnHand().floatValue());

		criteria = Criteria.create();
		condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentTypeProperty.getName());
		condition.setCondVal(item03.getDocumentType());
		condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentEntryProperty.getName());
		condition.setCondVal(item03.getDocumentEntry());
		condition = criteria.getConditions().create();
		condition.setAlias(MaterialsQuantityJournal.BaseDocumentLineIdProperty.getName());
		condition.setCondVal(item03.getDocumentLineId());
		operationResult = boRepository.fetchMaterialsQuantityJournal(criteria);
		assertEquals(operationResult.getMessage(), operationResult.getResultCode(), 0);
		journal01 = (MaterialsQuantityJournal) operationResult.getResultObjects().firstOrDefault();
		assertNull(journal01);

	}

	public void testWriteBO2DB() throws ComputeException, RepositoryException {
		/**
		 * 测试写入速度，建议添加以下配置项
		 * 
		 * <add key="DisabledRefetch" value="true" />
		 * <add key="DisabledBusinessLogics" value="true" />
		 * <add key="DisabledBusinessApproval" value="true" />
		 * <add key="DisabledCache" value="true" />
		 * <add key="DisabledVersionCheck" value="true" />
		 * <add key="DisabledPostTransaction" value="true" />
		 * <add key="EnabledUserFields" value="false" />
		 */
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.beginTransaction();
		DateTime start = DateTime.getNow();
		int count = 1000;
		for (int i = 0; i < count; i++) {
			SalesOrder order = new SalesOrder();
			order.setCustomerName("大量数据写入");
			ISalesOrderItem line = order.getSalesOrderItems().create();
			line.setItemDescription("鬼知道的物料1");
			line = order.getSalesOrderItems().create();
			line.setItemDescription("鬼知道的物料2");
			IOperationResult<?> operationResult = boRepository.saveSalesOrder(order);
			if (operationResult.getResultCode() != 0) {
				System.err.println(operationResult.getMessage());
				break;
			}
		}
		// boRepository.commitTransaction();
		// boRepository.dispose();
		DateTime finish = DateTime.getNow();
		System.out.println(String.format("写入[%s]条数据，从[%s]到[%s]共[%s]秒，平均%s条/秒。", count, start.toString("HH:mm:ss"),
				finish.toString("HH:mm:ss"), DateTime.interval(start, finish, emTimeUnit.second),
				count / DateTime.interval(start, finish, emTimeUnit.second)));
		SalesOrder order = new SalesOrder();
		order.setCustomerName("大量数据写入");
		ISalesOrderItem line = order.getSalesOrderItems().create();
		line.setItemDescription("鬼知道的物料1");
		line = order.getSalesOrderItems().create();
		line.setItemDescription("鬼知道的物料2");
		IOperationResult<?> operationResult = boRepository.saveSalesOrder(order);
		if (operationResult.getResultCode() != 0) {
			System.err.println(operationResult.getMessage());
		}
		line = order.getSalesOrderItems().create();
		line.setItemDescription("鬼知道的物料3");
		line = order.getSalesOrderItems().create();
		line.setItemDescription("鬼知道的物料4");
		operationResult = boRepository.saveSalesOrder(order);
		if (operationResult.getResultCode() != 0) {
			System.err.println(operationResult.getMessage());
		}
	}
}
