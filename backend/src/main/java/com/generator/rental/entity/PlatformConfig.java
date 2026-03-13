package com.generator.rental.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("platform_configs")
public class PlatformConfig extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey; // Mapped from config_key
    private String configValue; // Mapped from config_value
    private String description;
    
    // Alias for compatibility if needed, but better to refactor usage
    public String getKey() { return configKey; }
    public void setKey(String key) { this.configKey = key; }
    public String getValue() { return configValue; }
    public void setValue(String value) { this.configValue = value; }
}
