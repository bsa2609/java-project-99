package hexlet.code.mapper;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.BeforeMapping;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(
        uses = {JsonNullableMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class UserMapper {
    @Autowired
    private PasswordEncoder passwordEncoder;

    public abstract UserDTO map(User model);
    public abstract User map(UserCreateDTO dto);
    public abstract void update(UserUpdateDTO dto, @MappingTarget User model);

    @BeforeMapping
    public void encryptPassword(UserCreateDTO data) {
        String hashedPassword = passwordEncoder.encode(data.getPassword());
        data.setPassword(hashedPassword);
    }

    @BeforeMapping
    public void encryptPassword(UserUpdateDTO data) {
        if (data.getPassword() != null && data.getPassword().isPresent()) {
            String hashedPassword = passwordEncoder.encode(data.getPassword().get());
            data.setPassword(JsonNullable.of(hashedPassword));
        }
    }
}
