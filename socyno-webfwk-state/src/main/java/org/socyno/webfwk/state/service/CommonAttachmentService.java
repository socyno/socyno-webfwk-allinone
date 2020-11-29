package org.socyno.webfwk.state.service;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.socyno.webfwk.state.model.CommonAttachementItem;
import org.socyno.webfwk.state.model.CommonAttachementPath;
import org.socyno.webfwk.state.module.tenant.SystemTenantDataSource;
import org.socyno.webfwk.util.context.ContextUtil;
import org.socyno.webfwk.util.context.SessionContext;
import org.socyno.webfwk.util.exception.PageNotFoundException;
import org.socyno.webfwk.util.exception.PreviewContentNotAllownedException;
import org.socyno.webfwk.util.exception.PreviewTooLargeException;
import org.socyno.webfwk.util.model.ObjectMap;
import org.socyno.webfwk.util.sql.AbstractDao;
import org.socyno.webfwk.util.sql.AbstractDao.ResultSetProcessor;
import org.socyno.webfwk.util.sql.SqlQueryUtil;
import org.socyno.webfwk.util.tool.CommonUtil;
import org.socyno.webfwk.util.tool.ConvertUtil;
import org.socyno.webfwk.util.tool.DataUtil;
import org.socyno.webfwk.util.tool.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.Data;
import lombok.experimental.Accessors;

public abstract class CommonAttachmentService {

    public static AbstractDao getDao() {
        return SystemTenantDataSource.getMain();
    }
    
    @Data
    @Accessors(chain = true)
    private static class UploadFile {
        private String field;
        private String name;
        private String path;
        private String contentType;
        private long size;
    }
    
    /**
     * 上传附件
     */
	public static List<CommonAttachementItem> upload(String type, MultipartHttpServletRequest req) throws Exception {
	    if (StringUtils.isBlank(type)) {
	        throw new IllegalArgumentException();
	    }
	    Map<String, List<MultipartFile>> files;
	    if ((files = req.getMultiFileMap()) == null || files.size() <= 0) {
	        return Collections.emptyList();
	    }
	    final List<UploadFile> uploaded = new ArrayList<>();
	    for (List<MultipartFile> fileset : files.values()) {
	        if (fileset == null || fileset.size() <= 0) {
	            continue;
	        }
	        for (MultipartFile file : fileset) {
	            String storePath = String.format("/form-attachements/%s/%s/%s", type,
                        DateFormatUtils.format(new Date(), "yyyy-MM"), DataUtil.randomGuid());
	            CommonSftpService.DEFAULT.put(file.getInputStream(),  storePath);
                uploaded.add(new UploadFile().setField(file.getName()).setSize(file.getSize())
                        .setName(file.getOriginalFilename()).setContentType(file.getContentType())
                        .setPath(storePath));
	        }
	    }
	    final List<Long> attachementsIds = new ArrayList<>();
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet r, Connection c) throws Exception {
                for (UploadFile upload : uploaded) {
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        "system_common_attachement", new ObjectMap()
                            .put("type",  type)
                            .put("name",  upload.getName())
                            .put("path",  upload.getPath())
                            .put("size",  upload.getSize())
                            .put("field", upload.getField())
                            .put("content_type", CommonUtil.ifNull(upload.getContentType(), ""))
                            .put("created_id", SessionContext.getUserId())
                            .put("created_by", SessionContext.getUsername())
                            .put("created_name", SessionContext.getDisplay())
                            .put("created_at", new Date())
                    ), new ResultSetProcessor() {
                        @Override
                        public void process(ResultSet r, Connection c) throws Exception {
                            r.next();
                            attachementsIds.add(r.getLong(1));
                        }
                        
                    });
                }
            }
        });
        return queryByIds(attachementsIds);
	}
	
	/**
	 * 上传通用流程表单附件
	 * @param req
	 * @return
	 * @throws Exception
	 */
    public static List<CommonAttachementItem> formUpload(String formName, MultipartHttpServletRequest req) throws Exception {
        return upload(String.format("form:%s", formName), req);
    }
	
    /**
     * 上传附件，并建立与表单的关联关系
     */
    public static void bindWithForm(String formName, Object formId, Long... attachementIds) throws Exception {
        if (StringUtils.isBlank(formName) || formId == null || StringUtils.isBlank(formId.toString())) {
            throw new IllegalArgumentException();
        }
        if (attachementIds == null || attachementIds.length <= 0) {
            return;
        }
        getDao().executeTransaction(new ResultSetProcessor() {
            @Override
            public void process(ResultSet r, Connection c) throws Exception {
                for (Long attachementId : attachementIds) {
                    if (attachementId == null) {
                        continue;
                    }
                    getDao().executeUpdate(SqlQueryUtil.prepareInsertQuery(
                        "system_form_attachement", new ObjectMap()
                            .put("form_name",       formName)
                            .put("form_id",         formId)
                            .put("attachement_id",  attachementId)
                    ));
                }
            }
        });
    }
	
    /**
     * DELETE FROM
     *     system_form_attachement
     * WHERE
     *     attachement_id = ?
     * AND
     *     form_name = ?
     * AND
     *     form_id = ?
     * ;
     */
    @Multiline
    private final static String SQL_DELETE_ATTACHEMENT= "X";
    
    /**
     * TODO: 暂时屏蔽附件的删除功能，后续将通过系统任务的方式批量定期清理
     */
    public static void delete(String targetFrom, Object targetId, Long attachementId) throws Exception {
//        if (attachementId == null || StringUtils.isBlank(targetFrom) 
//                || targetId == null || StringUtils.isBlank(targetId.toString())) {
//            return;
//        }
//        getDao().executeUpdate(SQL_DELETE_ATTACHEMENT, new Object[] {attachementId, targetFrom, targetId});
    }
	
    /**
     * SELECT
     *     a.*
     * FROM
     *     system_common_attachement a
     * LEFT JOIN
     *     system_form_attachement f ON f.attachement_id = a.id
     * WHERE
     *     a.id = ?
     */
    @Multiline
    private final static String SQL_QUERY_FORM_ATTACHEMENTS_BY_ID = "X";
    
    private static CommonAttachementPath get(long attachementId, String targetFrom, Object targetId)
            throws Exception {
        List<Object> sqlArgs = new ArrayList<>();
        StringBuilder sqlStmt = new StringBuilder(SQL_QUERY_FORM_ATTACHEMENTS_BY_ID);
        if (StringUtils.isNotBlank(targetFrom) ) {
            sqlArgs.add(targetFrom);
            sqlStmt.append(" AND a.type = ?");
            
        }
        if (targetId != null && StringUtils.isNotBlank(targetId.toString()) && !"0".equals(targetId.toString())) {
            sqlArgs.add(targetId);
            sqlStmt.append(" AND f.form_id = ?");
        }
        sqlArgs.add(0, attachementId);
        CommonAttachementPath attchement;
        if ((attchement = getDao().queryAsObject(CommonAttachementPath.class, sqlStmt.toString(),
                sqlArgs.toArray())) == null) {
            throw new PageNotFoundException();
        }
        return attchement;
    }
    
    /**
     * 附件下载
     */
    public static void download(long attachementId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        CommonAttachementPath attchement = get(attachementId, null, null);
        attchement.setContentType("application/octet-stream");
        download(attchement, req, resp);
    }
    
    /**
     * 附件预览
     */
    public static void preview(long attachementId, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        CommonAttachementPath attchement = get(attachementId, null, null);
        if (attchement.getSize() > CommonUtil.parseLong(ContextUtil.
                getConfigTrimed("system.attachement.preview.maxsize"), 1024 * 5000L)) {
            throw new PreviewTooLargeException();
        }
        if (StringUtils.isBlank(attchement.getContentType()) || !ArrayUtils.contains(CommonUtil.split(
                    ContextUtil.getConfigTrimed("system.attachement.preview.allowns"),
                    "[,;\\s]+", CommonUtil.STR_LOWER|CommonUtil.STR_NONBLANK|CommonUtil.STR_TRIMED
                ), attchement.getContentType().trim().toLowerCase())){
            throw new PreviewContentNotAllownedException();
        }
        download(attchement, req, resp);
    }
    
    private static void download(CommonAttachementPath attchement, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String fileName = attchement.getName();
        String userAgent = StringUtils.trimToEmpty(req.getHeader("User-Agent")).toUpperCase();
        // IE/Edge浏览器
        if (userAgent.contains("MSIE") || (userAgent.contains("GECKO") && userAgent.contains("RV:11"))) {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        }
        // 其他浏览器
        else {
            fileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");  
        }
        resp.reset();
        resp.setContentType(attchement.getContentType());
        resp.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        CommonSftpService.DEFAULT.download(attchement.getPath(), resp.getOutputStream());
    }
    
    /**
     * SELECT
     *     a.*,
     *     f.form_id,
     *     f.form_name
     * FROM
     *     system_form_attachement f,
     *     system_common_attachement a
     * WHERE
     *     f.attachement_id = a.id
     * AND
     *     f.form_name = ?
     * AND
     *     a.field = ?
     * AND
     *     f.form_id in (%s)
     *
     * ;
     */
    @Multiline
    private final static String SQL_QUERY_FORM_ATTACHEMENTS_BY_FORM = "X";
    
    /**
     * 批量检索指定流程表单的关联字段附件
     * 
     * @param targetForm
     * @param targetField
     * @param targetIds
     * @return
     * @throws Exception
     */
    public static List<CommonAttachementItem> queryByTargetFormFeild(String targetForm, String targetField,
            Object... targetIds) throws Exception {
        return queryByTargetFormFeild(CommonAttachementItem.class, targetForm, targetField, targetIds);
    }
    
    public static <T extends CommonAttachementItem> List<T> queryByTargetFormFeild(Class<T> clazz, String targetForm,
            String targetField, Object... targetIds) throws Exception {
        if (StringUtils.isBlank(targetForm) || StringUtils.isBlank(targetField) || targetIds == null
                || targetIds.length <= 0) {
            return Collections.emptyList();
        }
        
        return getDao().queryAsList(clazz,
                String.format(SQL_QUERY_FORM_ATTACHEMENTS_BY_FORM, CommonUtil.join("?", targetIds.length, ",")),
                ArrayUtils.addAll(new Object[] { targetForm, targetField }, targetIds));
    }
    
    /**
     * DELETE f.* FROM
     *     system_form_attachement f,
     *     system_common_attachement a
     * WHERE
     *     f.attachement_id = a.id
     * AND
     *     f.form_name = ?
     * AND
     *     f.form_id = ?
     * AND
     *     a.field = ?
     * ;
     */
    @Multiline
    private final static String SQL_CLEAR_FORM_ATTACHEMENTS_BY_FORM = "X";
    /**
     * 清通用流程表单指定字段的附件
     * @param targetForm   表单的名称
     * @param targetId     表单的编号
     * @param targetField  表单的字段
     * @return
     * @throws Exception
     */
    public static void cleanByTargetFormField(String targetForm, Object targetId, String targetField) throws Exception {
        if (StringUtils.isBlank(targetForm) || StringUtils.isBlank(targetField) || targetId == null
                || StringUtils.isBlank(targetId.toString())) {
            return;
        }
        getDao().executeUpdate(SQL_CLEAR_FORM_ATTACHEMENTS_BY_FORM, new Object[] { targetForm, targetId, targetField });
    }
    
    /**
     * SELECT DISTINCT
     *     t.*
     * FROM
     *     system_common_attachement t
     * WHERE
     *     t.id in (%s)
     * ORDER BY
     *     t.id DESC
     * ;
     */
    @Multiline
    private final static String SQL_QUERY_ATTACHEMENTS_BY_IDS = "X";
    

    public static List<CommonAttachementItem> queryByIds(Long... ids) throws Exception {
        if (ids == null || (ids = ConvertUtil.asNonNullUniqueLongArray(ids)).length <= 0) {
            return Collections.emptyList();
        }
        return getDao().queryAsList(CommonAttachementItem.class,
                String.format(SQL_QUERY_ATTACHEMENTS_BY_IDS, CommonUtil.join("?", ids.length, ",")),
                ids);
    }
    
    public static List<CommonAttachementItem> queryByIds(Collection<Long> ids) throws Exception {
        if (ids == null || ids.size() <= 0) {
            return Collections.emptyList();
        }
        return queryByIds(ids.toArray(new Long[0]));
    }
}
