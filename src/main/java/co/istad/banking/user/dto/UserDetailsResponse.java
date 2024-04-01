package co.istad.banking.user.dto;

import co.istad.banking.domain.Role;

import java.util.List;

public record UserDetailsResponse(
//        Integer pin,
//        String phoneNumber,
//        String password,
//        String confirmedPassword,
        String  name,
        String gender,
        String dob,
        String nationalCard,
        String studentIdCard,
        List<RoleNameResponse> roles
) {

}
