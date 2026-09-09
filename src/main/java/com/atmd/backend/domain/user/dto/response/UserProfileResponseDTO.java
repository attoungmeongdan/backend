package com.atmd.backend.domain.user.dto.response;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDTO {

    private Long id;
    private String email;
    private String nickname;
    private String provider;
    private Integer age;
    private String gender;
    private Double height;
    private Double weight;
    private AddressResponse address;

    public static UserProfileResponseDTO from(User user) {
        Address addr = user.getAddress();
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .provider(user.getProvider().name())
                .age(user.getAge())
                .gender(user.getGender() != null ? user.getGender().name() : null)
                .height(user.getHeight())
                .weight(user.getWeight())
                .address(addr != null ? AddressResponse.from(addr) : null)
                .build();
    }

    @Getter
    @Builder
    public static class AddressResponse {
        private Long id;
        private String roadNameAddress;
        private String lotNumberAddress;
        private String detailAddress;
        private Double lat;
        private Double lng;

        public static AddressResponse from(Address address) {
            return AddressResponse.builder()
                    .id(address.getId())
                    .roadNameAddress(address.getRoadNameAddress())
                    .lotNumberAddress(address.getLotNumberAddress())
                    .detailAddress(address.getDetailAddress())
                    .lat(address.getLat())
                    .lng(address.getLng())
                    .build();
        }
    }
}
