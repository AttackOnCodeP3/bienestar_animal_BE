package com.project.demo.logic.entity.animal;

import com.project.demo.logic.entity.community_animal.CommunityAnimalRepository;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import com.project.demo.logic.entity.user.User;
import com.project.demo.logic.entity.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnimalRecordsService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private CommunityAnimalRepository comunityAnimalRepository;
    @Autowired
    private RoleRepository roleRepository;



    public Animal findAnimalRecordByIdAndUserId(Long animalId, Long userId) {

        Optional<User> userOpt = userRepository.findById(userId);

        List<RoleEnum> userRoles = userOpt.map(user -> userRepository.findRoleNamesByUserId(user.getId()))
                .orElse(List.of());

        // Check if the user has the COMMUNITY_USER role

        if (userRoles.contains(RoleEnum.COMMUNITY_USER)) {
            return (Animal) animalRepository.findCommunityAnimalsByUserId(userId);
        }


        return null;
    }
}
