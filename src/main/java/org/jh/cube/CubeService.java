package org.jh.cube;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CubeService {
    private final CubeProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CubeService(CubeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
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
            Boolean convertWebp,
            Boolean useUuid) throws IOException {
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

        String response = restClient.post()
                .uri("/api/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(String.class);

        CubeUploadResponse resp;
        try {
            resp = objectMapper.readValue(response, CubeUploadResponse.class);
        } catch (Exception e) {
            log.error("上传响应解析失败，返回内容: {}", response, e);
            throw new CubeException();
        }

        if (resp == null) {
            throw new CubeException();
        }
        if (resp.getData() == null) {
            throw new CubeException(resp.getCode(), resp.getMsg());
        }
        return resp.getObjectKey();
    }

    /**
     * 删除文件
     *
     * @param objectKey 文件路径
     */
    public void deleteFile(String objectKey) {
        String response = restClient.delete()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/delete")
                        .queryParam("bucket", properties.getBucketName())
                        .queryParam("object_key", objectKey)
                        .build())
                .retrieve()
                .body(String.class);

        CubeUploadResponse resp;
        try {
            resp = objectMapper.readValue(response, CubeUploadResponse.class);
        } catch (Exception e) {
            log.error("删除响应解析失败，返回内容: {}", response, e);
            throw new CubeException();
        }

        if (resp == null) {
            throw new CubeException();
        }
        if (resp.getCode() != 200) {
            throw new CubeException(resp.getCode(), resp.getMsg());
        }
    }

    /**
     * 拼接获取文件的 URL
     *
     * @param objectKey 文件路径
     * @param thumbnail 是否缩略图
     * @return 完整的文件访问 URL
     */
    public String getFileUrl(String objectKey, Boolean thumbnail) {
        String encodedKey = URLEncoder.encode(objectKey, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();

        sb.append(properties.getBaseUrl())
                .append("/api/file?bucket=")
                .append(properties.getBucketName())
                .append("&object_key=")
                .append(encodedKey);

        if (Boolean.TRUE.equals(thumbnail)) {
            sb.append("&thumbnail=true");
        }

        return sb.toString();
    }
}