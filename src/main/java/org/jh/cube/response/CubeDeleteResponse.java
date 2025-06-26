package org.jh.cube.response;

import lombok.Data;

/**
 * @author SugarMGP
 */
@Data
public class CubeDeleteResponse {
    private String msg;
    private Integer code;
    private Void data;
}
