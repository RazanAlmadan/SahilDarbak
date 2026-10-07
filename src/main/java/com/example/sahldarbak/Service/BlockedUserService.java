package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.BlockedUser;
import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.BlockedUserRepository;
import com.example.sahldarbak.Repository.ProfileRepository;
import com.example.sahldarbak.Repository.TravelMatchRepository;
import com.example.sahldarbak.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Repository.ProfileRepository;
import com.example.sahldarbak.DTO.BlockedUserDTO;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockedUserService {
    private final BlockedUserRepository blockedUserRepository;
    private final UserRepository userRepository;
    private final TravelMatchRepository travelMatchRepository;
    private final ProfileRepository profileRepository;

//    CRUD without update is no need for it

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

        // end every connection between the two users (pending and accepted, both directions)
        for (String status : List.of("pending", "accepted")) {
            TravelMatch sent = travelMatchRepository
                    .findTravelMatchBySenderIdAndReceiverIdAndStatus(blockerId, blockedId, status);
            if (sent != null)
                travelMatchRepository.delete(sent);

            TravelMatch received = travelMatchRepository
                    .findTravelMatchBySenderIdAndReceiverIdAndStatus(blockedId, blockerId, status);
            if (received != null)
                travelMatchRepository.delete(received);
        }

        blockedUserRepository.save(blockedUser);
    }

    public void delete(Integer id) {
        BlockedUser blockedUser = blockedUserRepository.findBlockedUserById(id);
        if (blockedUser == null)
            throw new ApiException("block record not found");

        blockedUserRepository.delete(blockedUser);
    }
// Extra endpoints

    // profiles of the users I blocked, with the block record id
    public List<BlockedUserDTO> getBlocked(Integer blockerId) {
        if (!userRepository.existsById(blockerId))
            throw new ApiException("user not found");

        List<BlockedUserDTO> result = new ArrayList<>();
        for (BlockedUser b : blockedUserRepository.findBlockedUsersByBlockerId(blockerId)) {
            Profile profile = profileRepository.findById(b.getBlocked().getId()).orElse(null);
            if (profile != null)
                result.add(new BlockedUserDTO(b.getId(), profile));
        }
        return result;
    }

    // true if either user blocked the other
    public boolean isBlocked(Integer userA, Integer userB) {
        if (!userRepository.existsById(userA) || !userRepository.existsById(userB))
            throw new ApiException("user not found");

        return blockedUserRepository.existsByBlockerIdAndBlockedId(userA, userB)
                || blockedUserRepository.existsByBlockerIdAndBlockedId(userB, userA);
    }

    public void unblock(Integer blockerId, Integer blockedId) {
        BlockedUser blockedUser = blockedUserRepository
                .findBlockedUserByBlockerIdAndBlockedId(blockerId, blockedId);
        if (blockedUser == null)
            throw new ApiException("this user is not blocked");

        blockedUserRepository.delete(blockedUser);
    }

}


