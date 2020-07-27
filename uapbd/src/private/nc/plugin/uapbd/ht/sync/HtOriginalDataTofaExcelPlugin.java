package nc.plugin.uapbd.ht.sync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import nc.bs.bd.util.DBAUtil;
import nc.bs.dao.BaseDAO;
import nc.bs.framework.common.RuntimeEnv;
import nc.bs.logging.Logger;
import nc.bs.pub.pa.PreAlertObject;
import nc.bs.pub.taskcenter.BgWorkingContext;
import nc.bs.pub.taskcenter.IBackgroundWorkPlugin;
//import nc.bs.uapbd.ht.HtInfConfig;
import nc.jdbc.framework.JdbcSession;
import nc.jdbc.framework.PersistenceManager;
import nc.jdbc.framework.SQLParameter;
import nc.jdbc.framework.exception.DbException;
import nc.jdbc.framework.processor.ArrayListProcessor;
import nc.jdbc.framework.processor.ColumnListProcessor;
import nc.vo.pub.BusinessException;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pubapp.AppContext;
import nc.vo.uapbd.ht.RepInterConfigVO;
import nc.vo.uapbd.ht.RepInterfaLogVO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import nc.bs.uapbd.ht.FtpFileManager;

public class HtOriginalDataTofaExcelPlugin implements IBackgroundWorkPlugin {
	public static final char CSVSEPARATOR = (char)16;
	public static final String separator = File.separator;
	private BaseDAO dao;
	public BaseDAO getDao(){
		if(dao==null){
			dao = new BaseDAO();
		}
		return dao;
	}
	private List<RepInterConfigVO> tableList;
	private List<String> tableNames;

	@Override
	public PreAlertObject executeTask(BgWorkingContext bgwc)
			throws BusinessException {
		String dateStr = AppContext.getInstance().getBusiDate().toStdString();
		Object dateObj = bgwc.getKeyMap().get("busidate");
		if(dateObj != null && !dateObj.toString().trim().isEmpty())
			dateStr = new UFDate(dateObj.toString()).toStdString();
		tableList = getOriginalDataTables();
		if(tableList==null||tableList.isEmpty()) return null;
		tableNames = getTableNames(tableList);
		
		createExcel(dateStr);
		bgwc.setLogStr("生成excel文件成功！");
		return null;
	}

	
	private static Map<String,String> confMap = new HashMap<String,String>(); 
	public Map<String,String> getFileConfMap() throws BusinessException{
		if(confMap == null || confMap.size() == 0){
			String configUrl = RuntimeEnv.getInstance().getNCHome() + File.separator + "ierp" + File.separator + "bin" + File.separator + "htfaconf.properties";
			File f = new File(configUrl);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				Properties properties = new Properties();
				properties.load(fis);
				String tmpfilepath = properties.getProperty("tmpfilepath") == null ? "" : properties.getProperty("tmpfilepath");
				String ftppath = properties.getProperty("ftppath") == null ? "" : properties.getProperty("ftppath");
				String ftpuser = properties.getProperty("ftpuser") == null ? "" : properties.getProperty("ftpuser");
				String ftppwd = properties.getProperty("ftppwd") == null ? "" : properties.getProperty("ftppwd");
				String groupcode = properties.getProperty("groupcode") == null ? "" : properties.getProperty("groupcode");
				String othtmpfilepath = properties.getProperty("othtmpfilepath") == null ? "" : properties.getProperty("othtmpfilepath");
				String facardtable = properties.getProperty("facardtable") == null ? "" : properties.getProperty("facardtable");
				String fileprefix = properties.getProperty("fileprefix") == null ? "" : properties.getProperty("fileprefix");


				
				confMap.put("tmpfilepath", tmpfilepath);
				confMap.put("ftppath", ftppath);
				confMap.put("ftpuser", ftpuser);
				confMap.put("ftppwd", ftppwd);
				confMap.put("groupcode", groupcode);
				confMap.put("othtmpfilepath", othtmpfilepath);
				confMap.put("facardtable", facardtable);
				confMap.put("fileprefix", fileprefix);

			} catch (FileNotFoundException e) {
				throw new BusinessException(e.getMessage(),e);
			} catch (IOException e) {
				throw new BusinessException(e.getMessage(),e);
			}finally{
				if(fis != null)
					try {
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

		}
		return confMap;
	}
	
	/**
	 * 创建txt文件，并上传
	 * @param columnNames 
	 * @param datas
	 * @param table
	 * @param dateStr
	 * @param zipName 
	 * @throws BusinessException
	 */
	private String createSingletxt( List<Object[]> datas, String table, String dateStr) throws BusinessException {
		String tmpFilepath = getFileConfMap().get("tmpfilepath")==null?"":getFileConfMap().get("tmpfilepath").toString();
		String fileprefix = getFileConfMap().get("fileprefix")==null?"":getFileConfMap().get("fileprefix").toString();//fileprefix=0700_FIN
		String filename =tmpFilepath +fileprefix+ table +dateStr.replace("-", "")+".txt";
		File txtFile = null;
		BufferedWriter txtWriter = null;
		try {
			txtFile = new File(filename);
			if (txtFile.exists()){
				//txtFile.delete();
				forceDelete(txtFile);
			}
			txtFile.createNewFile();
//			txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile),"GB2312"), 1024);
			txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"), 1024);
//			writeRow(columnNames.toArray(new Object[]{}), txtWriter); //写txt头
			for (Object[] row : datas)
				writeRow(row, txtWriter);
			txtWriter.flush();

		} catch (Exception e) {
			throw new BusinessException(e.getMessage(),e);
		} finally {
			try {
				txtWriter.close();
			} catch (IOException e) {
				Logger.error(e.getMessage());
			}
		}
		return table+"/"+ (datas.size()+1);
	}
	private List<Object> getColumns(String table) throws BusinessException {
		String sql = "select  column_name  from fa_user_col_comments  where Table_Name = '"+table+"' order by column_name";
		List<Object> columns = (List<Object>) getDao().executeQuery(sql, new ColumnListProcessor());
//		if(columns == null || columns.size() == 0)
//			throw new BusinessException("表"+table+"没有列信息，请检查");
		
		return columns;
	}
	/**
	 * 生成excel文件
	 * @param dateStr
	 * @param batch_no 
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	private void createExcel(String dateStr) throws BusinessException {
		//deletefile();
		List<String> tabinfs = new ArrayList<String>();
//		readOthExcels(tmpFilepath, tabinfs);
		List<RepInterfaLogVO> logs = new ArrayList<RepInterfaLogVO>();
		for(RepInterConfigVO vo : tableList){	
			String table = vo.getMidtablename();		
			List<Object> columnNames =getColumns(table);//null;
			String sql = getSql(columnNames,table);
			List<Object[]> datas = (List<Object[]>) getDao().executeQuery(sql, new ArrayListProcessor());
//			if(datas == null || datas.size() == 0) continue;
			String tabinf = createSingleExcel(columnNames,datas,vo,dateStr);
//			createSingletxt(datas, vo.getMidtablename(),dateStr);
			try {
				Thread.sleep(5000);
				createOkFile(tabinf,vo.getMidtablename(), dateStr);
			} catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			tabinfs.add(tabinf);
			RepInterfaLogVO log = genRepInterLog(dateStr,table);
			if(tabinf==null||"".equals(tabinf)){
				log.setExp_status("1");
			}
			logs.add(log);
		}
		//String loginfo = createLogtxt(dateStr,batch_no);
		//tabinfs.add(loginfo);
//		createOkFile(tabinfs,dateStr);
		if(logs!=null&&!logs.isEmpty()) insertVOs(logs);
	}
	
	private void createOkFile(String tabinfs,String tablename,String dateStr) throws BusinessException {
//		deleteOkfiles();
		
		String tmpFilepath = getFileConfMap().get("tmpfilepath")==null?"":getFileConfMap().get("tmpfilepath").toString();

		String fileprefix = getFileConfMap().get("fileprefix")==null?"":getFileConfMap().get("fileprefix").toString();//fileprefix=0700_FIN
		String filename=fileprefix +tablename;

		String okname = tmpFilepath + filename+ dateStr.replace("-", "")+".ok";
		File txtFile = null;
		BufferedWriter txtWriter = null;
		try {
			txtFile = new File(okname);
			if (txtFile.exists()){
				//txtFile.delete();
				forceDelete(txtFile);
			}
			txtFile.createNewFile();
			//txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "GB2312"), 1024);
			txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"), 1024);
			
			txtWriter.write(filename+CSVSEPARATOR+"执行成功");
		
//			txtWriter.write("TABLE_COUNT="+tabinfs);
			txtWriter.flush();

		} catch (Exception e) {
			throw new BusinessException(e.getMessage(),e);
		} finally {
			try {
				txtWriter.close();
			} catch (IOException e) {
				Logger.error(e.getMessage());
			}
		}
	}

	


	

	
	private String getSql(List<Object> columnNames, String table) throws BusinessException {
		StringBuilder sqlSb = new StringBuilder();
		sqlSb.append("select ");
		if(columnNames!=null&&columnNames.size()>0){
			for(Object columnName : columnNames)
				sqlSb.append(columnName).append(", ");
		}else{
			sqlSb.append(" * ").append(", ");

		}
		
		String sql = sqlSb.substring(0, sqlSb.length()-2);
		return sql + " from "+table ;
	}


	private void deleteOkfiles() throws BusinessException {
		try {
			String tmpFilepath = getFileConfMap().get("tmpfilepath")==null?"":getFileConfMap().get("tmpfilepath").toString();
			if (tmpFilepath == null || tmpFilepath.isEmpty())
				throw new BusinessException("请先配置excel文件暂存目录");
			File file = new File(tmpFilepath);
			String[] filelist = file.list();
			if(filelist == null || filelist.length == 0) return ;
			for (int i = 0; i < filelist.length; i++) {
				File delfile = new File(tmpFilepath + separator + filelist[i]);
				if(delfile.isFile()&&(delfile.getAbsolutePath().endsWith(".ok"))){
					forceDelete(delfile);
				}
			}
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}

	
	private boolean forceDelete(File file) {
        boolean result = file.delete();
        int tryCount = 0;
        while (!result && tryCount++ < 10) {
            System.gc();    //回收资源
            result = file.delete();
        }
        return result;
    }
	

	/**
	 * 创建excel文件，并上传
	 * @param columnNames 
	 * @param datas
	 * @param table
	 * @param dateStr
	 * @param zipName 
	 * @throws BusinessException
	 */
	private String createSingleExcel(List<Object> columnNames, List<Object[]> datas, RepInterConfigVO vo, String dateStr) throws BusinessException {
		String tmpFilepath = getFileConfMap().get("tmpfilepath")==null?"":getFileConfMap().get("tmpfilepath").toString();
		String fileprefix = getFileConfMap().get("fileprefix")==null?"":getFileConfMap().get("fileprefix").toString();//fileprefix=0700_FIN
		String table = vo.getMidtablename();	
		String filename = tmpFilepath +fileprefix+ table +dateStr.replace("-", "")+ ".csv";
		if(vo.getFileprefix()!=null&&!"".equals(vo.getFileprefix())){
			filename = tmpFilepath + vo.getFileprefix() + table + ".csv";
		}
		try {
//			writeExcel_xlsx(columnNames,datas,filename);
//			writeCsv
			writeCsv(columnNames,datas,filename);
		} catch (Exception e) {
			throw new BusinessException(e.getMessage(),e);
		}
		return table+"/"+ (datas.size()+1);
	}	
	
	/**
	 * 生成excel文件
	 * @param columnNames
	 * @param datas
	 * @param xlsxPath
	 * @throws BusinessException
	 */
	private void writeCsv(List<Object> columnNames, List<Object[]> datas, String xlsxPath) throws BusinessException{
		if (xlsxPath == null || xlsxPath.equals("")) {
			throw new BusinessException("文件路径不能为空");
		}
		File txtFile = null;
		BufferedWriter txtWriter = null;
		try {
			txtFile = new File(xlsxPath);
			if (txtFile.exists()){
				//txtFile.delete();
				forceDelete(txtFile);
			}
			txtFile.createNewFile();
			
			if(datas!=null&&datas.size()>0){
//				txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile),"GB2312"), 1024);
				txtWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtFile), "UTF-8"), 1024);
				if(columnNames!=null&&columnNames.size()>0){
				System.out.println("joijo");
				    writeRow(columnNames.toArray(new Object[]{}), txtWriter); //写txt头
				}
				
				for (Object[] row : datas){
					writeRow(row, txtWriter);
				}
				
				txtWriter.flush();
			}
			
		

		} catch (Exception e) {
			throw new BusinessException(e.getMessage(),e);
		} finally {
			try {
				if(txtWriter!=null){
					txtWriter.close();

				}
			} catch (IOException e) {
				Logger.error(e.getMessage());
			}
		}
	}
	/**
	 * 生成excel文件
	 * @param columnNames
	 * @param datas
	 * @param xlsxPath
	 * @throws BusinessException
	 */
	private void writeExcel_xlsx(List<Object> columnNames, List<Object[]> datas, String xlsxPath) throws BusinessException{
		if (xlsxPath == null || xlsxPath.equals("")) {
			throw new BusinessException("文件路径不能为空");
		}
		FileOutputStream outputStream = null;
		try {
			XSSFWorkbook wb = null;
			File file = new File(xlsxPath);
			if (file.exists()) {
				//file.delete();
				forceDelete(file);
				wb = new XSSFWorkbook();
			} else {
				wb = new XSSFWorkbook();
			}
			if (wb == null) return;
			
			if(datas!=null&&datas.size()>0){
				Sheet sheet = wb.createSheet("sheet1");
				XSSFCellStyle style = wb.createCellStyle();
				int rownum=0;
				if(columnNames!=null&&columnNames.size()>0){
					Row r = sheet.createRow(rownum);
					
					StringBuilder sb = new StringBuilder();
					
					for (Object data : columnNames) 
						sb.append(data == null ? "" : data.toString()).append(CSVSEPARATOR);
					
					
					if(sb.length() > 0){
						String result = sb.toString();
						result = result.substring(0, result.length()-1);
						
					
						Cell cell = r.createCell(0);
						cell.setCellStyle(style);
//						cell.setCellType(cell.CELL_TYPE_STRING);
						cell.setCellValue(result==null?"":result.toString());
					}
//					for(int i=0;i<columnNames.size();i++){
//						Cell cell = r.createCell(i);
//						cell.setCellStyle(style);
//						cell.setCellValue(columnNames.get(i)==null?"":columnNames.get(i).toString());
//					}
				}
				for (int i = 0; i < datas.size(); i++) {
					Row r = sheet.createRow(i+rownum);
					Object[] objects = datas.get(i);
					
//					char[] ss=new char[]{ 20 };  //CSVSEPARATOR
//					String separatorstr=new String (ss);
//					String gbk = new String(separatorstr.getBytes("gbk"), "utf-8"); 
//					 System.out.println(gbk);//
					StringBuilder sb = new StringBuilder();
					for (Object data : objects) {
						sb.append(data == null ? "" : data.toString()).append(CSVSEPARATOR);
					}
					
					if(sb.length() > 0){
						String result = sb.toString();
						result = result.substring(0, result.length()-1);
						
						
						Cell cell = r.createCell(0);
						cell.setCellStyle(style);
//						cell.setCellType(cell.CELL_TYPE_STRING);
						cell.setCellValue(result==null?"":result.toString());
					}
//					for(int j=0;j<objects.length;j++){
//						Cell cell = r.createCell(j);
//						cell.setCellStyle(style);
//						cell.setCellValue(objects[j]==null?"":objects[j].toString());
//					}
				}
			}
			
			outputStream = new FileOutputStream(xlsxPath);
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			Logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if(outputStream!=null)
					outputStream.close();
			} catch (IOException e) {
				Logger.error(e.getMessage());
			}
		}
	}

	/**
	 * 将数据写入txt文件
	 * @param row
	 * @param txtWriter
	 * @throws IOException
	 */
	private void writeRow(Object[] row, BufferedWriter txtWriter)throws IOException {
		StringBuilder sb = new StringBuilder();
		if(row!=null&&row.length>0){
		   for (int i=0;i<row.length;i++) {
			Object data=row[i];
			sb.append(data == null ? "" : data.toString()).append(CSVSEPARATOR);
		  }
		}
		if(sb.length() > 0){
			String result = sb.toString();
			result = result.substring(0, result.length()-1);
			txtWriter.write(result);
			//txtWriter.newLine();
			txtWriter.write("\r\n");
		}
	}



	private RepInterfaLogVO genRepInterLog(String dateStr, String table){
		RepInterfaLogVO logVO = new RepInterfaLogVO();
		logVO.setMidtablename(table);
		logVO.setFilename(dateStr.replace("-", ""));
		logVO.setExp_status("0");
		logVO.setExp_ts(new UFDateTime());
		logVO.setDr(0);
		if(tableList!=null&&!tableList.isEmpty()){
			for(RepInterConfigVO confvo:tableList){
				if(confvo.getMidtablename().equals(table)){
					logVO.setRptcode(confvo.getRptcode());
					logVO.setRptname(confvo.getRptname());
					break;
				}else {
					continue;
				}
			}
		}
		return logVO;
	}
	/**
	 * 获取需要导出的数据表列表
	 * @return
	 * @throws BusinessException
	 */
	private List<RepInterConfigVO> getOriginalDataTables() throws BusinessException{
		String fatablename=getFileConfMap().get("facardtable")==null?"":getFileConfMap().get("facardtable").toString();
		if(!"".equals(fatablename)){
			String[] tablenames=fatablename.split("-");
			List<RepInterConfigVO> listvo=new ArrayList<RepInterConfigVO>();
			for (int i = 0; i < tablenames.length; i++) {
				String string = tablenames[i];
				RepInterConfigVO vo=new RepInterConfigVO();
				vo.setMidtablename(string);
				listvo.add(vo);
				
			}
			return listvo;
		}
		return null;
	}
	private List<String> getTableNames(List<RepInterConfigVO> tableList){
		if(tableList==null||tableList.isEmpty())return null;
		List<String> list = new ArrayList<String>();
		for(RepInterConfigVO vo:tableList){
			if(vo.getMidtablename()!=null&&!"".equals(vo.getMidtablename()))
				list.add(vo.getMidtablename());
		}
		if(list!=null&&!list.isEmpty())
			return list;
		return null;
	}
	/**
	 * 记录日志
	 * @param vos
	 */
	private void insertVOs(List<RepInterfaLogVO> vos) {
		StringBuffer insertSqls = new StringBuffer();
		insertSqls.append(" insert into rep_interfalog (pk_interconfig,rptcode,rptname,midtablename,filename,batch_no,exp_status,exp_ts,dr)");
		insertSqls.append(" values (?,?,?,?,?,?,?,?,?)");
		SQLParameter sqlP = new SQLParameter();
		PersistenceManager sessionManager = null;
		try {
			sessionManager = PersistenceManager.getInstance();
		} catch (DbException e) {
			e.printStackTrace();
		}
		JdbcSession session = sessionManager.getJdbcSession();
		String[] strIDs = DBAUtil.getIdGenerator().generate(vos.size());
		try {
			int i = 0;
			for (RepInterfaLogVO vo : vos) {
				sqlP = new SQLParameter();
				sqlP.addParam(strIDs[i++]);//pk_interconfig
				sqlP.addParam(vo.getRptcode());//rptcode
				sqlP.addParam(vo.getRptname());//rptname
				sqlP.addParam(vo.getMidtablename());				
				sqlP.addParam(vo.getFilename());
				sqlP.addParam(vo.getBatch_no());
				sqlP.addParam(vo.getExp_status());
				sqlP.addParam(vo.getExp_ts());
				sqlP.addParam(0);
				session.addBatch(insertSqls.toString(), sqlP);
			}
			session.executeBatch();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
