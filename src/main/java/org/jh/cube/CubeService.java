package org.jh.cube;

import org.jh.cube.response.CubeDeleteResponse;
import org.jh.cube.response.CubeUploadResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author SugarMGP
 */
public class CubeService {
    private final CubeProperties properties;
    private final RestClient restClient;

    public CubeService(CubeProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Key", properties.getApiKey())
                .build();
    }

    /**
     * 上传文件
     *
     * @param file        文件
     * @param location    存储路径（可选）
     * @param convertWebp 是否转换为 webp 格式
     * @param useUuid     是否使用 UUID 作为文件名
     * @return 返回的 ObjectKey
     */
    public String uploadFile(
            MultipartFile file,
            String location,
            boolean convertWebp,
            boolean useUuid) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }

            @Override
            public long contentLength() {
                return file.getSize();
            }
        });
        body.add("bucket", properties.getBucketName());
        if (location != null && !location.isEmpty()) {
            body.add("location", location);
        }
        body.add("convert_webp", convertWebp);
        body.add("use_uuid", useUuid);

        CubeUploadResponse resp = restClient.post()
                .uri("/api/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(CubeUploadResponse.class)
                .getBody();
        if (resp == null) {
            throw new CubeException(200500, "上传失败");
        }
        if (resp.getData() == null) {
            throw new CubeException(resp.getCode(), resp.getMsg());
        }
        return resp.getObjectKey();
    }

    /**
     * 删除文件
     *
     * @param bucket    存储桶
     * @param objectKey 文件路径
     */
    public void deleteFile(String bucket, String objectKey) {
        CubeDeleteResponse resp = restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/delete")
                        .queryParam("bucket", bucket)
                        .queryParam("object_key", objectKey)
                        .build())
                .retrieve()
                .toEntity(CubeDeleteResponse.class)
                .getBody();
        if (resp == null) {
            throw new CubeException(200500, "删除失败");
        }
        if (resp.getCode() != 200) {
            throw new CubeException(resp.getCode(), resp.getMsg());
        }
    }

    /**
     * 拼接获取文件的 URL
     *
     * @param objectKey 文件路径
     * @return 完整的文件访问 URL
     */
    public String getFileUrl(String objectKey) {
        String key = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        return properties.getBaseUrl() + "/api/file?bucket=" + properties.getBucketName() + "&object_key=" + key;
    }
}