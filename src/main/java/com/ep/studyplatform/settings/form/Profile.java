package com.ep.studyplatform.settings.form;

import com.ep.studyplatform.domain.Account;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor // 생성자에 파라미터가 있는 경우, 컨트롤러에서 바인딩이 되지 않는 경우를 생각해서 기본 생성자를 만들 필요가 있다.
public class Profile {

    // 프로필과 관련된 정보
    @Length(max = 35)
    private String bio;

    // 개인 URL
    @Length(max = 50)
    private String url;

    // 직업
    @Length(max = 50)
    private String occupation;

    // 거주지
    @Length(max = 50)
    private String location;

    private String profileImage;

}
