package nc.web.ncsso;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nc.bs.logging.Logger;
import nc.pub.ncsso.tool.NcssoTool;
import nc.vo.pub.BusinessException;
import nc.web.ncsso.bs.AbstractSsoBS;
import nc.web.ncsso.bs.NcSsoBS;
import nc.web.ncsso.bs.PortalSsoBS;
import nc.web.ncsso.gs.GSZQTool;
import nc.web.ncsso.sse.SSETool;

@SuppressWarnings({ "restriction", "serial" })
public class NCSsoServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}

	@SuppressWarnings("unused")
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
		String systype = req.getParameter(Tools.SYS_TYPE_NAME);
		if (systype == null || systype.trim().length() == 0) {
			systype = req.getParameter(Tools.SYS_TYPE_NAME2);
		}

		
		String classType = NcssoTool.readInfoFromNcsso().getProperty("classType", "0");

		Tools tool = null;
		int ctype = Integer.parseInt(classType);
		switch (ctype) {
		case 0:
			tool = new Tools(req);
			break;
		/*case 1:
			tool = new SSETool(req);// 上交所
			break;*/
/*		case 1:
			tool = new HHJKTool(req);// 翰华金控
			break;
		case 2:
			tool = new LARSTool(req);// 利安人寿
			break;
		case 3:
			//tool = new GDXTTool(req);// 光大信托、民生电商
			break;
		case 4:
			tool = new HXBXTool(req);//华夏保险
			break;*/
		case 5:{
			//国盛证券
			try {
				//加载参数
				GSZQTool.initConfig();
			} catch (BusinessException e) {
				handleException(e,resp);
				return;
			}
			tool=new GSZQTool(req);
			break;
		}
		default:
			tool = new Tools(req);
			break;
		}

		String userName = tool.getUserName();
		try {
			if (!tool.isMatch()) {
				 throw new  BusinessException("");
			}
		} catch (BusinessException e) {
			handleException(e,resp);
			return;
		}

		try {
			AbstractSsoBS ssobs = null;
			if (Tools.SYS_YER.equals(systype)) {
				ssobs = new PortalSsoBS(tool);
				resp.sendRedirect(ssobs.getSsoUrl());// portal单点登录
			} else {
				ssobs = new NcSsoBS(tool);
				resp.sendRedirect(ssobs.getSsoUrl());// NC单点登录
			}
		} catch (Exception e) {
			handleException(e,resp);
		}
	}
	public void handleException(Exception e,ServletResponse res){
		try {
			PrintWriter pw = res.getWriter();
			pw.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\"><html><head ><title>ERROR</title></head><body style='text-align:center'><h1>ERROR</h1><p>");
			pw.append(	  e.getMessage());
			pw.append("</p></body></html>");
			 pw.flush();
		} catch (IOException e1) {
		}
		
	}
}