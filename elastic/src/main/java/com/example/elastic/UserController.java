package com.example.elastic;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {
    private UserDocumentRepository userDocumentRepository;

    public UserController(UserDocumentRepository userDocumentRepository) {
        this.userDocumentRepository = userDocumentRepository;
    }

    @PostMapping()
    public UserDocument createUser(@RequestBody UserCreateRequestDto requestDto) {
        UserDocument user = new UserDocument(
                requestDto.getId(),
                requestDto.getName(),
                requestDto.getAge(),
                requestDto.getIsActive()
        );
        return userDocumentRepository.save(user);
    }

    @GetMapping()
    public Page<UserDocument> findUsers() {
        return userDocumentRepository.findAll(PageRequest.of(0, 10));
    }
    @GetMapping("/{id}")
    public UserDocument findUserById(@PathVariable String id) {
        return userDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
    }

    @PutMapping("/{id}")
    public UserDocument updateUser(
            @PathVariable String id,
            @RequestBody UserUpdateRequestDto userUpdateRequestDto
    ){
        UserDocument existingUser = userDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        existingUser.setAge(userUpdateRequestDto.getAge());
        existingUser.setName(userUpdateRequestDto.getName());
        existingUser.setIsActive(userUpdateRequestDto.getIsActive());
        return userDocumentRepository.save(existingUser);
    }
    @DeleteMapping()
    public void deleteUser(@PathVariable String id) {
        UserDocument existingUser = userDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        userDocumentRepository.delete(existingUser);
    }
}
