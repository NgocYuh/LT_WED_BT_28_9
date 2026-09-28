package vn.hnhstore.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roleName", source = "role.name")
    UserView toView(User user);

    @Mapping(target = "roleName", source = "role.name")
    @Mapping(target = "productCount", ignore = true)
    UserAdminView toAdminView(User user);
}
