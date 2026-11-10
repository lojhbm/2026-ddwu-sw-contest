package com.contest.una.notice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class NoticeCreateRequestDto {
    private MultipartFile image;
    private String userLectureId;
}
