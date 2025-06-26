package org.jh.cube.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author SugarMGP
 */
@Data
public class CubeUploadResponse {
    private String msg;
    private Integer code;
    private UploadData data;

    public String getObjectKey() {
        return data.objectKey;
    }

    @Data
    static class UploadData {
        @JsonProperty("object_key")
        private String objectKey;
    }
}
