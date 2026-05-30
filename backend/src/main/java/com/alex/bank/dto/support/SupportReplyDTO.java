package com.alex.bank.dto.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportReplyDTO {

    private Long messageId;

    private String replyText;

}
