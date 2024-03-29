package co.istad.banking.mapper;

import co.istad.banking.domain.User;
import co.istad.banking.user.dto.UserCreateRequest;
import co.istad.banking.user.dto.UserDetilasResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    /* formaular
    * SourceType = UserCreateRequest (Parameter)
    * TargetType = User (UserReturn)
    * */

    User fromUserCreateRequest(UserCreateRequest userCreateRequest);
    void fromUserCreateRequest2(@MappingTarget User user, UserCreateRequest userCreateRequest);
    UserDetilasResponse toUserDetailsResponse(User user);
}