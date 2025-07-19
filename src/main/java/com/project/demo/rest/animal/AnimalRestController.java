package com.project.demo.rest.animal;

import com.project.demo.logic.entity.animal.AnimalRepository;
import com.project.demo.logic.entity.canton.CantonRepository;
import com.project.demo.logic.entity.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for general animal-related operations.
 * Currently does not handle abandoned animal registration (handled by AbandonedAnimalRestController).
 *
 * @author gjimenez
 */
@RestController
@RequestMapping("/animals")
public class AnimalRestController {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private CantonRepository cantonRepository;

    @Autowired
    private UserRepository userRepository;

    private final Logger logger = LoggerFactory.getLogger(AnimalRestController.class);

}
