package org.king2.sl.common.utils;

import io.minio.MinioClient;
import org.king2.sl.common.exceptions.CheckValueException;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/*=======================================================
	说明:    Minio文件服务器的操作工具类

	作者		时间					注释
  	鹿七		2019.08.06   			创建
=======================================================*/
public class MinioUtil {


    // 定义minio的url
    private String minioUrl;
    // 定义minio的用户名
    private String username;
    // 定义minio的密码
    private String password;
    // 定义添加的桶名称
    private String bucketName;

    public MinioUtil(String minioUrl, String username, String password, String bucketName) {

        // 判断信息
        if (StringUtils.isEmpty(minioUrl)) {
            throw new RuntimeException("MinIo服务器地址不能为空");
        } else if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new RuntimeException("MinIo的用户名和密码不能为空");
        } else if (StringUtils.isEmpty(bucketName)) {
            throw new RuntimeException("需要操作的桶名称不能为空");
        }

        this.minioUrl = minioUrl;
        this.username = username;
        this.password = password;
        this.bucketName = bucketName;
    }


    /**
     * -----------------------------------------------------
     * 功能:  上传图片的方法
     * <p>
     * 参数:
     * multipartFile            MultipartFile           图片对象
     * fileName                 String                  图片名称
     * fileType                 String                  文件类型
     * <p>
     * 返回: SystemResult               返回调用者的数据
     * -----------------------------------------------------
     */
    public SystemResult uploadFile(MultipartFile multipartFile, String fileName) throws Exception {

        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        MinioClient minioClient = new MinioClient(minioUrl, username, password);

        // 查询桶是否已经存在
        // 检查存储桶是否已经存在
        boolean isExist = minioClient.bucketExists(bucketName);
        // 不存在就创建
        if (!isExist) minioClient.makeBucket(bucketName);

        // 上传文件
        minioClient.putObject(bucketName, fileName, multipartFile.getInputStream(), multipartFile.getContentType());
        // 上传成功查询图片地址
        String url = minioClient.getObjectUrl(bucketName, fileName);
        return new SystemResult(url);
    }

    /**
     * 对上传的图片进行校验
     *
     * @param file
     * @return
     */
    public static String checkUploadFile(MultipartFile file) throws IOException, CheckValueException {
        if (file == null) {
            throw new CheckValueException("图片不能为空");
        } else if (file.getInputStream() == null) {
            throw new CheckValueException("图片不能为空");
        } else if (file.getBytes() == null || file.getBytes().length <= 0) {
            throw new CheckValueException("图片不能为空");
        }

        StringBuffer fileName = new StringBuffer();
        String fileNameFix = MD5Utils.md5("IMAGE_" + UUID.randomUUID().toString() +
                System.currentTimeMillis());

        String originalFilename = file.getOriginalFilename();
        String fileNameEnd = originalFilename.substring(originalFilename.lastIndexOf("."));
        fileName.append(fileNameFix).append(fileNameEnd);
        return fileName.toString();
    }

    /**
     * -----------------------------------------------------
     * 功能:  删除文件的方法
     * <p>
     * 参数:
     * delFileName            String           需要删除的文件名称
     * <p>
     * 返回: SystemResult               返回调用者的数据
     * -----------------------------------------------------
     */
    public SystemResult delFile(String delFileName) throws Exception {
//        // 使用MinIO服务的URL，端口，Access key和Secret key创建一个MinioClient对象
        MinioClient minioClient = new MinioClient(minioUrl, username, password);
        minioClient.removeObject(bucketName, delFileName);
        return new SystemResult("ok");
    }

}
