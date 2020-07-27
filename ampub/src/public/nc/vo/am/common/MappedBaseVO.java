package nc.vo.am.common;

import java.util.ArrayList;
import java.util.List;

import nc.vo.aim.equip.DeptscaleVO;
import nc.vo.am.common.util.BaseVOUtils;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.ValidationException;

/**
 * HashMap化的基准VO对象。
 * 
 * @author ghj
 * 
 * @since nc5.7 update by taorz1 原有的解决方案可能会使内存泄露，
 */
public class MappedBaseVO extends SuperVO {

	private List<String> keysList = new ArrayList<String>();

	private ArrayList<Object> valuesMap = new ArrayList<Object>();

	/** 对应的数据库表名 */
	private String tableName = null;

	/** 主键字段 */
	private String pKFieldName = null;

	/** 父键字段 */
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
	 * 直接转换成Hash化的VO对象。
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

			// 设置主键。
			setAttributeValue(superVO.getPKFieldName(), superVO.getPrimaryKey());
			// 设置返回的时间戳。
			setAttributeValue("ts", baseVO.getAttributeValue("ts"));
			// update by taorz1：增加状态字段
			setAttributeValue("status", baseVO.getAttributeValue("status"));
			// 设置回写的状态
			setAttributeValue("bill_status", baseVO.getAttributeValue("bill_status"));
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					// 忽略主键和ts字段。
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
	 * 转换成相应的具体的VO对象。
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
		// 如果value为空， 则只增加attributeName, Map中不放置value值。
		// 这样可以大幅度减少数据传输的流量，提高数据传输的效率。
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
		// 注释掉if条件 于2008-10-20 zhaoss1
		// 由于在账表汇总的时候每个attribute都需要存在与MappedBaseVO中，即使他的值为空，这样才能保证汇总
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
