package com.weimob.webfwk.util.vcs.svn;

import lombok.extern.slf4j.Slf4j;

import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNDiffStatus;

import com.weimob.webfwk.util.tool.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public abstract class SubversionApiService {
    
    public abstract SVNRepository getRepository() throws SVNException;
    
    public SVNNodeKind checkPath(String svnPath) {
        return checkPath(svnPath, -1);
    }
    
    public SVNNodeKind checkPath(String svnPath, long revision ) {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.checkPath(repository, StringUtils.trim(svnPath), revision);
        } catch ( Exception e) {
            log.error(e.toString(), e);
            return null;
        }
        finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public long createDir(String svnRepoPath, String commitMessage) throws SVNException {
        return createDir(svnRepoPath, commitMessage, null);
    }
    
    public long createDir(String svnRepoPath, String commitMessage, String resetAuthor) throws SVNException {
        SVNRepository repository = getRepository();
        try {
            return SubversionUtil.createDir(repository, svnRepoPath, commitMessage, resetAuthor);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public void copy(String sourcePath,String targetPath, Long version, String message) throws SVNException {
        SVNRepository repository = getRepository();
        try {
            SVNClientManager client = SubversionUtil.getClientManager(repository);
            SubversionUtil.copy(client, sourcePath, targetPath, version,message);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
	}
    
    public long deleteFile(String svnPath, String commitMessage) throws SVNException {
        return deleteFile(svnPath, commitMessage, null);
    }
    
    public long deleteFile(String svnPath, String commitMessage, String resetAuthor) throws SVNException {
        SVNRepository repository = getRepository();
        try {
            return SubversionUtil.deleteFile(repository, svnPath, commitMessage, resetAuthor);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public boolean dirExists(String svnPath ) {
        return SVNNodeKind.DIR.equals(checkPath(svnPath));
    }
    
    public boolean dirExists(String svnPath, long revision ) {
        return SVNNodeKind.DIR.equals(checkPath(svnPath, revision));
    }
    
    public boolean fileExists(String svnPath) {
        return SVNNodeKind.FILE.equals(checkPath(svnPath));
    }
    
    public boolean fileExists(String svnPath, long revision ) {
        return SVNNodeKind.FILE.equals(checkPath(svnPath, revision));
    }
    
    public Collection<SVNLogEntry> getLogs(String svnPath, long startRevision) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.getLogs(repository, svnPath, startRevision, -1L, -1);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    /* 获取最新的日志 */
    public Collection<SVNLogEntry> getLogs(String svnPath, int limit) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.getLogs(repository, svnPath, limit);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    /* 获取指定版本间的日志， 包含给定版本 */
    public Collection<SVNLogEntry> getLogs(String svnPath, long startRevision, long endRevision) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.getLogs(repository, svnPath, startRevision, endRevision);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    /* 获取指定版本之前的日志， 包含给定版本 */
    public Collection<SVNLogEntry> getLogs(String svnPath, long endRevision, int limit) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.getLogs(repository, svnPath, endRevision, limit);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    /**
     * 获取两个不同版本间的差异信息
     * 
     * @param svnPath
     * @param newRev   新的版本。 如果 <=0 即 HEAD。
     * @param oldRev   老的版本
     * @return
     * @throws SVNException
     */
    public List<SVNDiffStatus> genDiffStatuses(String svnPath, long newRev, long oldRev) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.genDiffStatuses(repository, svnPath, newRev, oldRev);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public long getMaxRevision() throws SVNException {
        SVNRepository repo = null;
        try {
            repo = getRepository();
            return SubversionUtil.getMaxRevision(repo);
        } finally {
            SubversionUtil.closeRepository(repo);
        }
    }
    
    public long getMaxRevision(String... svnNameOrPaths) throws SVNException {
        Long maxRevision = 0L;
        for (String path : svnNameOrPaths) {
            Collection<SVNLogEntry> logs = getLogs(path, 1);
            for (SVNLogEntry svnLogEntry : logs) {
                long revision = svnLogEntry.getRevision();
                if(maxRevision < revision){
                    maxRevision = revision;
                }
            }
        }
        return maxRevision;
    }
    
    /**
     * 获取指定的版本属性值
     * 
     * @param revision
     * @param property
     * @return
     * @throws SVNException
     */
    public SVNPropertyValue getRevisionProperty(long revision, String property)
            throws SVNException {
        SVNRepository repo = getRepository();
        try {
            return repo.getRevisionPropertyValue(revision, property);
        } finally {
            SubversionUtil.closeRepository(repo);
        }
    }
    
    /**
     * 获取指定的版本属性
     * 
     * @param revision
     * @return
     * @throws SVNException
     */
    public SVNProperties getRevisionProperties(long revision)
            throws SVNException {
        SVNRepository repo = getRepository();
        try {
            return repo.getRevisionProperties(revision, null);
        } finally {
            SubversionUtil.closeRepository(repo);
        }
    }
    
    /**
     * 在同一会话中批量执行SVN操作，可提高多次操作的效率，且更加灵活
     * @param proccessor      执行器，由使用者实现抽象的 run 方法
     * @throws Exception
     */
    public void batchProcess(SubversionProcessor proccessor) throws Exception {
        SVNRepository repo = getRepository();
        try {
            proccessor.run(repo);
        } finally {
            SubversionUtil.closeRepository(repo);
        }
    }
    
    public Collection<SVNDirEntry> listDir(String svnPath) throws SVNException {
        return listDir(svnPath, -1);
    }
    
    public Collection<SVNDirEntry> listDir(String svnPath, long revision) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.getDir(repository, StringUtils.trim(svnPath), revision);
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public boolean pathExists(String svnPath ) {
        SVNNodeKind node = checkPath(svnPath);
        return node != null && !SVNNodeKind.NONE.equals(node);
    }
    
    public boolean pathExists(String svnPath, long revision ) {
        SVNNodeKind node = checkPath(svnPath, revision);
        return node != null && !SVNNodeKind.NONE.equals(node);
    }
    
    public byte[] readFromFile(String svnPath) {
        return readFromFile(svnPath, -1);
    }
    
    public byte[] readFromFile(String svnPath, long revision ) {
        try {
            return readFromFile2(svnPath, revision);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 读取SVN文本内容
     * @param svnPath
     * @param revision DEFAULT NULL
     * @param encoding DEFAULT UTF-8
     * @return
     */
    public String readFromTextFile(String svnPath, Long revision, String encoding) {
    	try {
    		if (revision == null) {
    			revision = -1L;
    		}
    		byte[] txtBytes = readFromFile2(svnPath, revision);
    		if (StringUtils.isBlank(encoding)) {
    			encoding = "UTF-8";
    		}
    		return new String(txtBytes, encoding);
    	} catch (Exception e) {
    		log.error(e.getMessage(), e);
    		return null;
    	}
    }
    
    public byte[] readFromFile2(String svnPath) throws Exception {
        return readFromFile2(svnPath, -1);
    }
    
    public byte[] readFromFile2(String svnPath, long revision ) throws Exception {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.readFromFile(repository, svnPath, revision);
        } catch (Exception e) {
            log.error( String.format(
                "Failed to fetch file content : %s",
                svnPath
            ), e );
            throw e;
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    public long appendToFile(String svnPath, byte[] data, String commitMessage ) {
        return appendToFile(svnPath, data, commitMessage, null);
    }
    
    public long appendToFile(String svnPath, byte[] data, String commitMessage, String resetAuthor ) {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.appendToFile(repository, svnPath, data, commitMessage, resetAuthor);
        } catch (Exception e) {
            log.error( String.format(
                "Failed to update file content : %s",
                svnPath
            ), e );
            return -1;
        } finally {
            SubversionUtil.closeRepository(repository);
        } 
    }
    
    public long writeToFile(String svnPath, byte[] data, String commitMessage) {
        return writeToFile(svnPath, data, commitMessage, null);
    }
    
    public long writeToFile(String svnPath, byte[] data, String commitMessage, String resetAuthor ) {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.writeToFile(repository, svnPath, data, commitMessage, resetAuthor);
        } catch (Exception e) {
            log.error( String.format(
                "Failed to update file content : %s",
                svnPath
            ), e );
            return -1;
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
    
    /**
     * 批量操作SVN文件或目录
     * 
     * @param pathsData
     *            相对路径.如果路径以 +++ 开头且不为文件（数据为null），表示内容追加; 如果路径以 ---
     *            开头且,表示删除目录或文件
     * @param commitMessage
     *            提交描述信息
     * @param resetAuthor
     *            提交人信息
     * @return 文件提交后在仓库的版本号（如果为目录且目录已经存在，返回仓库最新版本号）
     */
    public long writeToFiles(Map<String, byte[]> pathsData, String commitMessage, String resetAuthor) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            return SubversionUtil.writeToFiles(repository, pathsData, commitMessage,
                    resetAuthor);
        } catch (Exception e) {
            log.error("Failed to upload files to subversion", e);
            return -1;
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }

    /**
     * 批量操作SVN文件或目录
     * 
     * @param pathsData
     *            相对路径.
     *            如果路径以 +++ 开头且为文件（数据不为null），表示内容追加;
     *            如果路径以 ---开头且, 表示删除目录或文件
     * @param commitMessage
     *            提交描述信息
     * @return 文件提交后在仓库的版本号（如果为目录且目录已经存在，返回仓库最新版本号）
     */
    public long writeToFiles(Map<String, byte[]> pathsData, String commitMessage) throws SVNException {
        return writeToFiles(pathsData, commitMessage, null);
    }
   
    /**
     * 设置 SVN revision 的属性
     * 
     * @param revision
     *            版本编号
     * @param propName
     *            属性名称
     * @param propValue
     *            属性内容
     */
    public void setRevisionProperty(long revision, String propName, String propValue) throws SVNException {
        SVNRepository repository = null;
        try {
            repository = getRepository();
            repository.setRevisionPropertyValue(revision,
                    propName, SVNPropertyValue.create(propValue));
        } finally {
            SubversionUtil.closeRepository(repository);
        }
    }
}
