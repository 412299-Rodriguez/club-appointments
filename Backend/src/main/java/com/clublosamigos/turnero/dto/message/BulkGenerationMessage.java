package com.clublosamigos.turnero.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for bulk training session generation tasks sent through RabbitMQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGenerationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long slotConfigurationId;
    private String initiatedBy;
}
