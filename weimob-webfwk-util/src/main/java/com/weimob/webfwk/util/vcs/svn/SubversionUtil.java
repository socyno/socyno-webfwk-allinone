package com.weimob.webfwk.util.vcs.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
//import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.SVNExternal;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.ISVNDiffStatusHandler;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCopySource;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.weimob.webfwk.util.remote.HttpUtil;
import com.weimob.webfwk.util.tool.ChecksumUtil;
import com.weimob.webfwk.util.tool.CommonUtil;


@Slf4j
public class SubversionUtil {
    
    private static SVNRevision createSVNRevision(Long revision) {
        return revision == null || revision <= 0 ? SVNRevision.HEAD : SVNRevision.create(revision);
    }
    
    
    
    public static String getLocation(SVNRepository svnRepo) {
        String location = svnRepo.getLocation().toDecodedString();
        while ((location = StringUtils.trimToEmpty(location)).endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }
        return location;
    }
    
    public static String getRootUrl(@NonNull SVNRepository svnRepo) throws SVNException {
        SVNURL url;
        if ((url = svnRepo.getRepositoryRoot(true)) != null) {
            String strurl = url.toDecodedString();
            while ((strurl = StringUtils.trimToEmpty(strurl)).endsWith("/")) {
                strurl = strurl.substring(0, strurl.length() - 1);
            }
            return strurl;
        }
        return null;
    }
    
    private static String concatPaths(String... svnPath) {
        if (svnPath == null || svnPath.length <= 0) {
            return "";
        }
        String concated = StringUtils.join(svnPath, "/").replaceAll("/+", "/");
        while ((concated = StringUtils.trimToEmpty(concated)).endsWith("/")) {
            concated = concated.substring(0, concated.length() - 1);
        }
        return concated;
    }
    
    public static String pathCanonicalize(SVNRepository svnRepo, String svnPath) throws SVNException {
        return pathCanonicalize(svnRepo, svnPath, false);
    }
    
    public static String pathCanonicalize(SVNRepository svnRepo, String svnPath, boolean asUrl) throws SVNException {
        String location = getRootUrl(svnRepo);
        if ((svnPath = StringUtils.trimToEmpty(svnPath)).equals(location)) {
            svnPath = "/";
        } else if (svnPath.startsWith(String.format("%s/", location))) {
            svnPath = svnPath.substring(location.length()).replaceAll("/+", "/");
        }
        if (!svnPath.startsWith("/")) {
            svnPath = "/" + svnPath;
        }
        return asUrl ? HttpUtil.concatUrlPath(location, svnPath) : svnPath;
    }
    
    public static String urlCanonicalize(SVNRepository svnRepo, String svnPath) throws SVNException {
        return pathCanonicalize(svnRepo, svnPath, true);
    }
    
    private static ISVNAuthenticationManager getAuthenticationManager(String user, String password) {
        return new DefaultSVNAuthenticationManager(null, false, user, password, null, null) {
            @Override
            public int getConnectTimeout(SVNRepository repository) {
                return 30000;
            }
            
            @Override
            public int getReadTimeout(SVNRepository repository) {
                return 60000;
            }
        };
    }
    
    public static SVNRepository getRepository(String svnUrl, String user, String password)
            throws SVNException {
        while ( (svnUrl=StringUtils.trimToEmpty(svnUrl)).endsWith("/") ) {
            svnUrl = svnUrl.substring(0, svnUrl.length() - 1);
        }
        FSRepositoryFactory.setup();
        SVNRepository repository = SVNRepositoryFactory.create(
                        SVNURL.parseURIEncoded(svnUrl));
        repository.setAuthenticationManager(getAuthenticationManager(user, password));
        repository.setLocation(SVNURL.parseURIEncoded(getRootUrl(repository)), true);
        return repository;
    }
    
    public static SVNClientManager getClientManager(SVNRepository repo) throws SVNException {
        ISVNOptions myOptions = SVNWCUtil.createDefaultOptions(true);
        return SVNClientManager.newInstance(myOptions, repo.getAuthenticationManager());
    }
    
    public static SVNDiffClient getDiffClient(@NonNull SVNRepository svnRepo) throws SVNException {
        return getClientManager(svnRepo).getDiffClient();
    }
    
    public static SVNWCClient getWcClient(@NonNull SVNRepository svnRepo) throws SVNException {
        return getClientManager(svnRepo).getWCClient();
    }
    
    public static SVNPropertyData getVersionedProperty(@NonNull SVNRepository svnRepo, String path, String propName)
            throws SVNException {
        return getVersionedProperty(svnRepo, path, propName, null);
    }
    
    public static SVNPropertyData getVersionedProperty(@NonNull SVNRepository svnRepo, String path, String propName, Long revision)
            throws SVNException {
        return getWcClient(svnRepo).doGetProperty(SVNURL.parseURIDecoded(urlCanonicalize(svnRepo, path)), propName,
                SVNRevision.UNDEFINED, createSVNRevision(revision));
    }
    
    public static Map<String, SVNExternal> getExternalsProperty(@NonNull SVNRepository svnRepo, String path)
            throws SVNException {
        return getExternalsProperty(svnRepo, path, null);
    }
    
    public static Map<String, SVNExternal> getExternalsProperty(@NonNull SVNRepository svnRepo, String path,
            Long revision) throws SVNException {
        SVNPropertyData svnExternals;
        if ((svnExternals = SubversionUtil.getVersionedProperty(svnRepo, path, SVNProperty.EXTERNALS, revision)) == null
                || svnExternals.getValue() == null) {
            return null;
        }
        Map<String, SVNExternal> externals = new HashMap<>();
        for (SVNExternal external : SVNExternal.parseExternals(path,
                SVNPropertyValue.getPropertyAsString(svnExternals.getValue()))) {
            externals.put(external.getPath(), external);
        }
        return externals;
    }
    
    public static long appendToFile(SVNRepository svnRepo, String svnPath, byte[] data, String commitMessage)
            throws SVNException {
        return appendToFile(svnRepo, svnPath, data, commitMessage, null);
    }
    
    public static long appendToFile(SVNRepository svnRepo, String svnPath, byte[] data, String commitMessage,
            String resetAuthor) throws SVNException {
        return createPath(svnRepo, String.format("+++%s", svnPath), data == null ? new byte[0] : data, commitMessage,
                resetAuthor);
    }
    
    /**
     * 复制文件或目录
     * @param client   客户端管理器
     * @param srcUrl   源地址
     * @param destUrl  目标地址
     * @return         完成后创建的版本号
     * @throws SVNException
     */
    public static long copy(SVNClientManager client, String srcUrl, String destUrl)
                throws SVNException  {
        return copy(client, srcUrl, destUrl, -1, false,null);
    }
    /**
     * 复制文件或目录
     * @param client   客户端管理器
     * @param srcUrl   源地址
     * @param destUrl  目标地址
     * @param revision 源地址版本号（如果小于等于0，即最新版本）
     * @return         完成后创建的版本号
     * @throws SVNException
     */
    public static long copy(SVNClientManager client, String srcUrl, String destUrl, long revision)
                throws SVNException  {
        return copy(client, srcUrl, destUrl, revision, false,null);
    }
    /**
     * 复制文件或目录
     * @param client
     * @param srcUrl
     * @param destUrl
     * @param revision
     * @param message
     * @return
     * @throws SVNException
     */
    public static long copy(SVNClientManager client, String srcUrl, String destUrl, long revision,String message) throws SVNException  {
        return copy(client, srcUrl, destUrl, revision,false,message);
    }
    
    /**
     * 创建本地的SVN仓库 
     **/
    public static void createLocalRepoisotroy(String path) throws SVNException {
        SVNRepositoryFactory.createLocalRepository(new File(path), true, true);
    }
    
    /**
     * 移动文件或目录
     * @param client   客户端管理器
     * @param srcUrl   源地址
     * @param destUrl  目标地址
     * @return         完成后创建的版本号
     * @throws SVNException
     */
    public static long move(SVNClientManager client, String srcUrl, String destUrl)
                throws SVNException  {
        return copy(client, srcUrl, destUrl, -1, true,null);
    }
    
    private static long copy(SVNClientManager client, String srcUrl, String destUrl, long revision, boolean srcRemoved,
            String message) throws SVNException {
        SVNRevision revisionX = revision <= 0 ? SVNRevision.HEAD : SVNRevision.create(revision);
        SVNCopySource srcCopy = new SVNCopySource(revisionX, revisionX, SVNURL.parseURIEncoded(srcUrl));
        SVNCommitInfo commitInfo = client.getCopyClient().doCopy(new SVNCopySource[] { srcCopy },
                SVNURL.parseURIEncoded(destUrl), srcRemoved, true, true,
                message == null ? String.format("Copy from %s@%s", srcUrl, revisionX.getName()) : message, null);
        return commitInfo.getNewRevision();
    }
    
    public static byte[] readFromFile(SVNRepository svnRepo, String svnPath )
            throws SVNException {
        return readFromFile(svnRepo, svnPath, -1);
    }
    
    public static byte[] readFromFile(SVNRepository svnRepo, String svnPath, long revision)
            throws SVNException {
        svnPath = pathCanonicalize(svnRepo, svnPath);
        if ( !SVNNodeKind.FILE.equals(checkPath(svnRepo, svnPath, revision)) ) {
            String errmsg = String.format(
                "Failed to fetch file content : %s, %s",
                svnPath, "not a regular file." );
            log.error(errmsg);
            throw new SVNException( SVNErrorMessage.create(
                SVNErrorCode.FS_NOT_FILE, errmsg));
        }
        log.debug("Svn path = {}, location : {}", 
                svnPath,
                svnRepo.getLocation().toDecodedString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        svnRepo.getFile(svnPath, revision, new SVNProperties(), baos);
        return baos.toByteArray();
    }

    public static long deleteEntry(SVNRepository svnRepo, String svnPath,
                        String commitMessage) throws SVNException{
        return deleteEntry(svnRepo, svnPath, commitMessage, null);
    }

    public static long deleteEntry(SVNRepository svnRepo, String svnPath,
            String commitMessage, String resetAuthor) throws SVNException{
        return createPath(svnRepo, String.format("---%s", svnPath),
                new byte[0],  commitMessage, resetAuthor );
    }
    
    public static long deleteFile(SVNRepository svnRepo, String svnPath,
                            String commitMessage) throws SVNException{
        return deleteEntry(svnRepo, svnPath, commitMessage, null);
    }
    
    public static long deleteFile(SVNRepository svnRepo, String svnPath,
            String commitMessage, String resetAuthor) throws SVNException{
        return deleteEntry(svnRepo, svnPath, commitMessage, resetAuthor);
    }
    
    public static SVNNodeKind checkPath(SVNRepository svnRepo, String svnPath) {
        return checkPath(svnRepo, svnPath, -1);
    }
    
    public static SVNNodeKind checkPath(SVNRepository svnRepo, String svnPath, long revision) {
        try {
            svnPath = pathCanonicalize(svnRepo, svnPath);
            return svnRepo.checkPath(svnPath, revision);
        } catch (SVNException e) {
            log.error(e.toString(), e);
            return null;
        }
    }
    
    public static void closeRepository(SVNRepository repo) {
        if (repo != null) {
            repo.closeSession();
        }
    }
    
    public static long getMaxRevision(SVNRepository svnRepo) throws SVNException {
        return svnRepo.getLatestRevision();
    }
    
    /**
     * 将数据内容写入到SVN指定文件
     * 
     * @param svnRepo       SVN仓库对象
     * @param svnPath       相对路径.如果路径以 +++ 开头且不为文件（数据为null），表示内容追加;
     *                            如果路径以 --- 开头且,表示删除目录或文件
     * @param data          需要写入的数据
     * @param commitMessage 提交描述信息
     * @param resetAuthor   提交人信息
     * @return              文件提交后在仓库的版本号
     * @throws SVNException
     */
    public static long writeToFile(SVNRepository svnRepo, String svnPath,
            byte[] data, String commitMessage, String resetAuthor ) throws SVNException {
        return createPath(svnRepo, svnPath, data == null ? new byte[0] : data,
                commitMessage, resetAuthor);
    }
    
    /**
     * 批量操作SVN文件或目录
     * @param svnRepo       SVN仓库对象
     * @param pathsData     相对路径.如果路径以 +++ 开头且不为文件（数据为null），表示内容追加;
     *                            如果路径以 --- 开头且,表示删除目录或文件
     * @param commitMessage 提交描述信息
     * @param resetAuthor   提交人信息
     * @return              文件提交后在仓库的版本号（如果为目录且目录已经存在，返回仓库最新版本号）
     */
    public static long writeToFiles(SVNRepository svnRepo,
            Map<String, byte[]> pathsData, String commitMessage, String resetAuthor )
                        throws SVNException {
        return createPath(svnRepo, pathsData, commitMessage, resetAuthor);
    }
    
    public static long createDir(SVNRepository svnRepo, String svnPath,
            String commitMessage ) throws SVNException {
        return createPath(svnRepo, svnPath, null, commitMessage, null);
    }
    
    public static long createDir(SVNRepository svnRepo, String svnPath,
            String commitMessage, String resetAuthor ) throws SVNException {
        return createPath(svnRepo, svnPath, null, commitMessage, resetAuthor);
    }
    
    /**
     * 创建 SVN 目录或文件
     * @param svnRepo       SVN仓库对象
     * @param svnPath       相对路径.如果路径以 +++ 开头且为文件（data!=null），表示内容追加;
     *                            如果路径以 --- 开头且,表示删除目录或文件
     * @param data          null 值意味着是创建目录，否则为文件内容数据
     * @param commitMessage 提交描述信息
     * @param resetAuthor   提交人信息
     * @return              文件提交后在仓库的版本号（如果为目录且目录已经存在，返回仓库最新版本号）
     */
    private static long createPath(SVNRepository svnRepo, String path, byte[] data,
            String commitMessage, String resetAuthor) throws SVNException {
        Map<String, byte[]> pd = new HashMap<String, byte[]>();
        pd.put(path, data);
        return createPath(svnRepo, pd, commitMessage, resetAuthor);
    }
    
    private static List<SubversionCommitNode> prepareCommitCreate(
            @NonNull SVNRepository svnRepo,
            @NonNull Map<String, byte[]> pathsData)throws SVNException {
        List<SubversionCommitNode> preparedNodes = new ArrayList<SubversionCommitNode>();
        for (Entry<String, byte[]> pd : pathsData.entrySet()) {
            SubversionCommitNode leafNode = new SubversionCommitNode();
            String svnPath = pd.getKey();
            leafNode.setData(pd.getValue());
            /* starts with --- means delete entry */
            /* starts with +++ means append content */
            if (svnPath != null) {
                if(svnPath.startsWith("+++") && !leafNode.isDirectory()) {
                    leafNode.setEntryOpType(SubversionEntryOpType.APPEND);
                    svnPath = svnPath.substring(3);
                } else if (svnPath.startsWith("---")) {
                    leafNode.setEntryOpType(SubversionEntryOpType.DELETE);
                    svnPath = svnPath.substring(3);
                }
            }
            svnPath = pathCanonicalize(svnRepo, svnPath);
            if (svnPath.isEmpty() || svnPath.equals("/")) {
                continue;
            }
            SVNNodeKind nodeKind;
            SubversionCommitNode parentNode = leafNode;
            SubversionCommitNode currentNode = leafNode;
            String[] pathNodes = StringUtils.split(svnPath,"/");
            for (int i = pathNodes.length; i > 0; i--) {
                String subPath = String.format("/%s", StringUtils.join(
                        pathNodes, "/", 0, i) );
                nodeKind = svnRepo.checkPath(subPath, -1);
                log.debug("Checking svn path : {}/{} - {} => {}",
                            i, pathNodes.length, subPath, nodeKind);
                if (i == pathNodes.length) {
                    leafNode.setName(pathNodes[i - 1]);
                    if (!SVNNodeKind.NONE.equals(nodeKind)) {
                        log.debug("\texisted");
                        leafNode.setPathExists(true);
                        if (!SubversionEntryOpType.DELETE.equals(leafNode.getEntryOpType())) {
                            if (!(SVNNodeKind.DIR.equals(nodeKind) && leafNode.isDirectory())
                                && !(SVNNodeKind.FILE.equals(nodeKind) && !leafNode.isDirectory())) {
                                throw new SVNException(SVNErrorMessage.create(
                                    SVNErrorCode.NODE_UNEXPECTED_KIND, String.format(
                                        "Path is unexcepted kind, path=%s, kind=%s",
                                        subPath, nodeKind
                                    )
                                ));
                            }
                            if (!leafNode.isDirectory()) {
                                leafNode.setOriginData(readFromFile(svnRepo, svnPath));
                            }
                        }
                    }
                } else {
                    parentNode = new SubversionCommitNode().setName(pathNodes[i-1])
                                    .addChild(currentNode);
                    currentNode = parentNode;
                    if (SVNNodeKind.NONE.equals(nodeKind)) {
                        continue;
                    }
                    if (!SVNNodeKind.DIR.equals(nodeKind)) {
                        String errmsg = String.format("Svn path '%s' not a directroy.",
                                                subPath);
                        log.error(errmsg);
                        throw new SVNException(SVNErrorMessage.create(
                            SVNErrorCode.NODE_UNEXPECTED_KIND, errmsg
                        ));
                    }
                    parentNode.setPathExists(true);
                    for (int j = i - 2; j >= 0; j--) {
                        parentNode = new SubversionCommitNode().setName(pathNodes[j])
                                        .setPathExists(true)
                                        .addChild(currentNode);
                        currentNode = parentNode;
                    }
                    break;
                }
            }
            preparedNodes.add(parentNode);
        }
        CommitNodesMerge(preparedNodes);
        return preparedNodes;
    }
    
    private static boolean processCommitCreate(
            @NonNull String basepath,
            @NonNull ISVNEditor editor,
            @NonNull SubversionCommitNode commitNode)throws SVNException {
        
        String name = commitNode.getName();
        List<SubversionCommitNode> children = commitNode.getChildren();
        if (!basepath.endsWith("/")) {
            basepath = basepath + "/";
        }
        String svnpath = basepath + name;
        if (SubversionEntryOpType.DELETE.equals(commitNode.getEntryOpType())) {
            log.info("Try to remove entry {}", svnpath);
            if (commitNode.isPathExists()) {
                editor.deleteEntry(svnpath, -1);
                log.debug("\t\t deleted");
                return true;
            }
            log.info("\t\t not exists and skipped");
            return false;
        }
        if (commitNode.isDirectory()) {
            boolean hasChanges = false;
            log.info("Try to add or open directory {}",
                            svnpath);
            if ("/".equals(svnpath)) {
                editor.openRoot(-1);
            } else if (commitNode.isPathExists()) {
                editor.openDir(svnpath, -1);
                log.info("\t\t opened");
            } else {
                hasChanges = true;
                editor.addDir(svnpath, null, -1);
                log.info("\t\t added");
            }
            if (children != null && !children.isEmpty()) {
                for (SubversionCommitNode c : children) {
                    if (processCommitCreate(svnpath, editor, c)) {
                        hasChanges = true;
                    }
                }
            }
            editor.closeDir();
            return hasChanges;
        } else {
            log.info("Try to create or change file {}", svnpath);
            if (commitNode.isPathExists()) {
                /* content is same, skipped */
                if (!SubversionEntryOpType.APPEND.equals(commitNode.getEntryOpType())
                        && commitNode.getOriginData() != null 
                        && commitNode.getData().length == commitNode.getOriginData().length
                        && ChecksumUtil.getSHA256(commitNode.getData())
                                        .equals(ChecksumUtil.getSHA256(commitNode.getOriginData()))) {
                    log.info("\t\t same contents and skipped");
                    return false;
                }
                editor.openFile(svnpath, -1);
                log.info("\t\t changed");
            } else {
                editor.addFile(svnpath, null, -1);
                log.info("\t\t added");
            }
            editor.applyTextDelta(svnpath, null);
            byte[] data = SubversionEntryOpType.APPEND.equals(commitNode.getEntryOpType())
                              && commitNode.getOriginData() != null
                                ? ArrayUtils.addAll(commitNode.getOriginData(), commitNode.getData())
                                : commitNode.getData();
            SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
            String checksum = deltaGenerator.sendDelta(svnpath,
                        new ByteArrayInputStream(data), editor, true);
            log.info("Uploaded file delta checksum = {}", checksum);
            editor.closeFile(svnpath, checksum);
            return true;
        }
    }

    /**
     * 创建 SVN 目录或文件
     * @param svnRepo       SVN仓库对象
     * @param pathsData     相对路径.如果路径以 +++ 开头且为文件（data!=null），表示内容追加;
     *                            如果路径以 --- 开头且,表示删除目录或文件
     * @param commitMessage 提交描述信息
     * @param resetAuthor   提交人信息
     * @return              文件提交后在仓库的版本号（如果为目录且目录已经存在，返回仓库最新版本号）
     */
    private static long createPath(
            @NonNull SVNRepository svnRepo,
            @NonNull Map<String, byte[]> pathsData,
            String commitMessage, String resetAuthor) throws SVNException {
        List<SubversionCommitNode> commitNodes = prepareCommitCreate(svnRepo, pathsData);
        log.debug("Merged commit nodes = {}", commitNodes);
        if (commitNodes.isEmpty()) {
            log.info("No file or directory need to upload, return latest' revision.");
            return svnRepo.getLatestRevision();
        } 
        
        /* Add OriginAuthor in commitMessage */
        commitMessage = CommonUtil.ifNull(commitMessage, "");
        if (!(resetAuthor=StringUtils.trimToEmpty(resetAuthor)).isEmpty()
                && commitMessage.toLowerCase().indexOf("origin-author") < 0) {
            commitMessage = String.format(
                "%s%nOrigin-Author:%s",
                commitMessage, resetAuthor
           );
        }
        SubversionCommitNode rootNode = new SubversionCommitNode().setName("")
                        .setPathExists(true)
                        .addChildren(commitNodes);
        ISVNEditor editor = svnRepo.getCommitEditor(
            commitMessage,
            null /* locks */,
            true /* keepLocks */,
            null /* mediator */
        );
        boolean hasChanges = false;
        SVNCommitInfo commit = null;
        try {
            if (hasChanges = processCommitCreate("", editor, rootNode)) {
                commit = editor.closeEdit();
            }
        } finally {
            if (commit == null || SVNCommitInfo.NULL.equals(commit)) {
                editor.abortEdit();
            }
        }
        long revision = hasChanges && commit != null ? commit.getNewRevision()
                            : svnRepo.getLatestRevision();
        log.debug("Commit submitted = {}, revision = {}", hasChanges, revision);
        /* Try to reset revision's author */
        if (commit != null && SVNCommitInfo.NULL.equals(commit)
                && StringUtils.isNotBlank(resetAuthor)
                && !resetAuthor.equals(commit.getAuthor())) {
            try {
                svnRepo.setRevisionPropertyValue(revision, "svn:author",
                        SVNPropertyValue.create(resetAuthor));
            } catch (Exception e) {
                log.warn("Failed to reset svn:author to {} on revision {}",
                        resetAuthor, revision);
                log.error(e.toString(), e);
            }
        }
        return revision;
    }

    public static List<SVNLogEntry> getLogs(@NonNull SVNRepository svnRepo,
            String svnPath, long startRevision, long endRevision )
                    throws SVNException {
        return getLogs(svnRepo, svnPath, startRevision, endRevision, Integer.MAX_VALUE);
    }
    
    public static List<SVNLogEntry> getLogs(@NonNull SVNRepository svnRepo,
            String svnPath, int limit )
                    throws SVNException {
        return getLogs(svnRepo, svnPath, -1, 0, limit);
    }
    
    public static List<SVNLogEntry> getLogs(@NonNull SVNRepository svnRepo,
            String svnPath, long endRevision,  int limit )
                    throws SVNException {
        return getLogs(svnRepo, svnPath, endRevision, 0, limit);
    }
    
    public static List<SVNLogEntry> getLogs(@NonNull SVNRepository svnRepo,
            String svnPath, long startRevision, long endRevision, int limit )
                    throws SVNException {
        svnPath = pathCanonicalize(svnRepo, svnPath);
        final List<SVNLogEntry> entries = new LinkedList<SVNLogEntry>();
        svnRepo.log(new String[] { svnPath },
                startRevision, endRevision, true, true, limit,
                new ISVNLogEntryHandler() {
                    @Override
                    public void handleLogEntry(SVNLogEntry e)
                            throws SVNException {
                        entries.add(e);
                    }
        } );
        return entries;
    }
    
    public static Collection<SVNDirEntry> getDir(
            @NonNull SVNRepository svnRepo,
            String svnPath ) throws SVNException {
        return getDir(svnRepo, svnPath, -1);
    }
    
    public static Collection<SVNDirEntry> getDir(@NonNull SVNRepository svnRepo, String svnPath, long revision)
            throws SVNException {
        svnPath = pathCanonicalize(svnRepo, svnPath);
        final Collection<SVNDirEntry> entries = new LinkedList<SVNDirEntry>();
//        svnRepo.getDir(svnPath, revision, null, new ISVNDirEntryHandler () {
//                 @Override
//                 public void handleDirEntry(SVNDirEntry e) throws SVNException {
//                       entries.add(e);
//                 }
//        } );
        svnRepo.getDir(svnPath, revision, true, entries);
        return entries;
    }
    
    public static Collection<SVNDirEntry> getDir(@NonNull SVNRepository svnRepo, String svnPath, long revision,
            boolean recursive) throws SVNException {
        Collection<SVNDirEntry> entries;
        svnPath = pathCanonicalize(svnRepo, svnPath);
        if ((entries = getDir(svnRepo, svnPath, revision)).size() > 0 && recursive) {
            String subPath;
            for (Object d : entries.toArray()) {
                if (!SVNNodeKind.DIR.equals(((SVNDirEntry) d).getKind())) {
                    continue;
                }
                subPath = concatPaths(svnPath, ((SVNDirEntry) d).getName());
                entries.addAll(getDir(svnRepo, subPath, revision, true));
            }
        }
        return entries;
    }
    
    public static Collection<SVNDirEntry> getAllFile(@NonNull SVNRepository svnRepo, String svnPath, long revision,
            boolean recursive) throws SVNException {
        Collection<SVNDirEntry> entries;
        Collection<SVNDirEntry> returnEntries = new ArrayList<>();
        svnPath = pathCanonicalize(svnRepo, svnPath);
        if ((entries = getDir(svnRepo, svnPath, revision)).size() > 0 && recursive) {
            String subPath;
            for (Object d : entries.toArray()) {
                if (SVNNodeKind.FILE.equals(((SVNDirEntry) d).getKind())) {
                    returnEntries.add((SVNDirEntry) d);
                } else if (SVNNodeKind.DIR.equals(((SVNDirEntry) d).getKind())) {
                    subPath = concatPaths(svnPath, ((SVNDirEntry) d).getName());
                    returnEntries.addAll(getAllFile(svnRepo, subPath, revision, true));
                }
            }
        }
        return returnEntries;
    }
    
    public static List<SVNDiffStatus> genDiffStatuses(@NonNull SVNRepository svnRepo, String svnPath, long newRev,
            long oldRev) throws SVNException {
        svnPath = urlCanonicalize(svnRepo, svnPath);
        SVNDiffClient diffClient = getDiffClient(svnRepo);
        final List<SVNDiffStatus> diffStatuses = new ArrayList<SVNDiffStatus>();
        log.info("Svn diff status r{}:{} {}", oldRev, newRev, svnPath);
        final SVNURL svnUrl = SVNURL.parseURIDecoded(svnPath);
        diffClient.doDiffStatus(svnUrl, SVNRevision.create(oldRev), svnUrl,
                newRev > 0 ? SVNRevision.create(newRev) : SVNRevision.HEAD, SVNDepth.INFINITY, false,
                new ISVNDiffStatusHandler() {
                    @Override
                    public void handleDiffStatus(SVNDiffStatus status) throws SVNException {
                        diffStatuses.add(status);
                    }
                });
        return diffStatuses;
    }
    
    private static void CommitNodesMerge(List<SubversionCommitNode> preparedNodes) {
        if (preparedNodes == null || preparedNodes.size() < 2) {
            return;
        }
        Collections.sort(preparedNodes, new Comparator<SubversionCommitNode>() {
            @Override
            public int compare(SubversionCommitNode l, SubversionCommitNode r) {
                return StringUtils.compare(l.getName(), r.getName());
            }
        });
        int nodesMaxIdx = preparedNodes.size() - 2;
        String name = preparedNodes.get(nodesMaxIdx + 1).getName();
        for (int i = nodesMaxIdx; i >= 0; i--) {
            SubversionCommitNode p = preparedNodes.get(i);
            if (StringUtils.equals(p.getName(), name)) {
                List<SubversionCommitNode> children = preparedNodes.remove(i + 1).getChildren();
                if (children != null) {
                    for (SubversionCommitNode c : children) {
                        p.addChild(c);
                    }
                }
            } else {
                CommitNodesMerge(preparedNodes.get(i + 1).getChildren());
            }
            name = p.getName();
        }
        CommitNodesMerge(preparedNodes.get(0).getChildren());
    }

    public static String adapterUrl(SVNRepository repo, String url) throws SVNException {
        String prefix = repo.getRepositoryRoot(false).toDecodedString();
        if (url == null || "".equals(url)) {
            return url;
        }

        if (!url.startsWith(prefix)) {
            return prefix + "/" + repo.getRepositoryPath(url);
        }
        return url;
    }
}
