package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.BlockedUser;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.BlockedUserRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockedUserService {
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;

    public List<BlockedUser> get(){ return blockedUserRepository.findAll();}

    public void add(Integer blockerId, Integer blockedId) {
        if (blockerId.equals(blockedId))
            throw new ApiException("you cannot block yourself");

        User blocker = userRepository.findUserById(blockerId);
        if (blocker == null)
            throw new ApiException("blocker user not found");

        User blocked = userRepository.findUserById(blockedId);
        if (blocked == null)
            throw new ApiException("blocked user not found");

        if (blockedUserRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId))
            throw new ApiException("this user is already blocked");

        BlockedUser blockedUser = new BlockedUser();
        blockedUser.setBlocker(blocker);
        blockedUser.setBlocked(blocked);
        blockedUser.setBlockedAt(LocalDate.now());

        blockedUserRepository.save(blockedUser);
    }


}
