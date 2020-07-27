package nc.vo.am.common;

import java.util.ArrayList;
import java.util.List;

import nc.vo.aim.equip.DeptscaleVO;
import nc.vo.am.common.util.BaseVOUtils;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.ValidationException;

/**
 * HashMap���Ļ�׼VO����
 * 
 * @author ghj
 * 
 * @since nc5.7 update by taorz1 ԭ�еĽ���������ܻ�ʹ�ڴ�й¶��
 */
public class MappedBaseVO extends SuperVO {

	private List<String> keysList = new ArrayList<String>();

	private ArrayList<Object> valuesMap = new ArrayList<Object>();

	/** ��Ӧ�����ݿ���� */
	private String tableName = null;

	/** �����ֶ� */
	private String pKFieldName = null;

	/** �����ֶ� */
	private String parentPKFieldName = null;

	public MappedBaseVO() {
	}

	public MappedBaseVO(CircularlyAccessibleValueObject baseVO) {
		this(baseVO, baseVO == null ? null : baseVO.getAttributeNames());
	}

	private int getValueIndex(String field) {		
		int index = keysList.indexOf(field);
		

		return index;
	}

	/**
	 * ֱ��ת����Hash����VO����
	 * 
	 * @param baseVO
	 */
	public MappedBaseVO(CircularlyAccessibleValueObject baseVO, String[] keys) {
		if (baseVO == null)
			return;

		if (baseVO instanceof SuperVO) {
			SuperVO superVO = (SuperVO) baseVO;
			setPKFieldName(superVO.getPKFieldName());
			setParentPKFieldName(superVO.getParentPKFieldName());
			setTableName(superVO.getTableName());

			// ����������
			setAttributeValue(superVO.getPKFieldName(), superVO.getPrimaryKey());
			// ���÷��ص�ʱ�����
			setAttributeValue("ts", baseVO.getAttributeValue("ts"));
			// update by taorz1������״̬�ֶ�
			setAttributeValue("status", baseVO.getAttributeValue("status"));
			// ���û�д��״̬
			setAttributeValue("bill_status", baseVO.getAttributeValue("bill_status"));
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					// ����������ts�ֶΡ�
					if (keys[i].equals(superVO.getPKFieldName()) || keys[i].equals("ts")) {
						continue;
					}
					setAttributeValue(keys[i], baseVO.getAttributeValue(keys[i]));
				}
			}
		} else {
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					setAttributeValue(keys[i], baseVO.getAttributeValue(keys[i]));
				}
			}
		}
	}

	/**
	 * ת������Ӧ�ľ����VO����
	 * 
	 * @param clazz
	 * @return
	 */
	public CircularlyAccessibleValueObject convert(Class clazz) {
		CircularlyAccessibleValueObject baseVO = (CircularlyAccessibleValueObject) BaseVOUtils.initClass(clazz);
		String[] keys = baseVO.getAttributeNames();
		for (int i = 0; i < keys.length; i++) {
			String key = keys[i];
			if (keysList.contains(key)) {
				baseVO.setAttributeValue(key, getAttributeValue(key));
			}
		}

		return baseVO;
	}

	public boolean containsKey(String key) {
		return keysList.contains(key);
	}

	@Override
	public String[] getAttributeNames() {
		return this.keysList.toArray(new String[0]);
	}

	@Override
	public Object getAttributeValue(String attributeName) {
		int valueIndex = getValueIndex(attributeName);

		if (-1 == valueIndex) {
			return null;
		} else {
			return valuesMap.get(valueIndex);
		}
	}
	
	@Override
	public void setAttributeValue(String attributeName, Object value) {
		// ���valueΪ�գ� ��ֻ����attributeName, Map�в�����valueֵ��
		// �������Դ���ȼ������ݴ����������������ݴ����Ч�ʡ�
		// ghj
		if(DeptscaleVO.PK_DEPTSCALE.equalsIgnoreCase(attributeName)){
			System.out.print("");
		}
		if (!keysList.contains(attributeName)) {
			keysList.add(attributeName);
			valuesMap.add(value);
		} else {
			valuesMap.set(getValueIndex(attributeName), value);
		}
		// ע�͵�if���� ��2008-10-20 zhaoss1
		// �������˱���ܵ�ʱ��ÿ��attribute����Ҫ������MappedBaseVO�У���ʹ����ֵΪ�գ��������ܱ�֤����
		// if (value != null) {
		// }
	}

	@Override
	public Object clone() {
		return (MappedBaseVO) super.clone();
	}

	@Override
	public String getEntityName() {
		return "none";
	}

	@Override
	public void validate() throws ValidationException {
	}

	@Override
	public String getPKFieldName() {
		return pKFieldName;
	}

	@Override
	public String getParentPKFieldName() {
		return parentPKFieldName;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setPKFieldName(String fieldName) {
		pKFieldName = fieldName;
	}

	public void setParentPKFieldName(String parentPKFieldName) {
		this.parentPKFieldName = parentPKFieldName;
	}

	@Override
	public String getPrimaryKey() {
		return (String) valuesMap.get(getValueIndex(pKFieldName));
	}

	@Override
	public void setPrimaryKey(String key) {
		valuesMap.set(getValueIndex(pKFieldName), key);
	}
}
