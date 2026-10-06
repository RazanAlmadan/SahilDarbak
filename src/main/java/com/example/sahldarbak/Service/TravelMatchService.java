package com.example.sahldarbak.Service;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.Model.Profile;
import com.example.sahldarbak.Model.TravelMatch;
import com.example.sahldarbak.Model.TravelPresence;
import com.example.sahldarbak.Model.User;
import com.example.sahldarbak.Repository.*;
import lombok.RequiredArgsConstructor;
import com.example.sahldarbak.DTO.ReceivedInviteDTO;
import com.example.sahldarbak.DTO.ContactDTO;
import org.springframework.stereotype.Service;
import com.example.sahldarbak.DTO.SentInviteDTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelMatchService {
    private final TravelMatchRepository travelMatchRepository;
    private final UserRepository userRepository;
    private final TravelPresenceRepository travelPresenceRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final ProfileRepository profileRepository;
    private final WhatsAppService whatsAppService;



// CRUD without get and delete

    // send an invite after you see the profiles given from the TravelPresence getMatches method
    public void add(Integer senderId, Integer receiverId, String message) {
        if (senderId.equals(receiverId))
            throw new ApiException("you cannot invite yourself");

        if (message == null || message.isBlank() || message.length() > 300)
            throw new ApiException("message is required and must not exceed 300 characters");

        User sender = userRepository.findUserById(senderId);
        if (sender == null)
            throw new ApiException("sender user not found");

        User receiver = userRepository.findUserById(receiverId);
        if (receiver == null)
            throw new ApiException("receiver user not found");

        // same-city gate
        TravelPresence senderPresence = travelPresenceRepository.findTravelPresenceById(senderId);
        if (senderPresence == null)
            throw new ApiException("check in to a city first");

        TravelPresence receiverPresence = travelPresenceRepository.findTravelPresenceById(receiverId);
        if (receiverPresence == null)
            throw new ApiException("this user is not checked in anywhere");

        if (!senderPresence.getCountry().equals(receiverPresence.getCountry())
                || !senderPresence.getCity().equals(receiverPresence.getCity()))
            throw new ApiException("you and this user are not in the same city");

        // block gate (both directions)
        if (blockedUserRepository.existsByBlockerIdAndBlockedId(senderId, receiverId)
                || blockedUserRepository.existsByBlockerIdAndBlockedId(receiverId, senderId))
            throw new ApiException("you cannot invite this user");

        // duplicate in the same direction (any status)
        if (travelMatchRepository.existsBySenderIdAndReceiverId(senderId, receiverId))
            throw new ApiException("you already sent an invite to this user");

        // reverse direction: block if pending or accepted
        if (travelMatchRepository.existsBySenderIdAndReceiverIdAndStatus(receiverId, senderId, "pending"))
            throw new ApiException("this user already sent you a pending invite, respond to it instead");

        if (travelMatchRepository.existsBySenderIdAndReceiverIdAndStatus(receiverId, senderId, "accepted"))
            throw new ApiException("you are already connected with this user");

        TravelMatch match = new TravelMatch();
        match.setSender(sender);
        match.setReceiver(receiver);
        match.setMessage(message);
        match.setStatus("pending");
        match.setCreatedAt(LocalDate.now());

        travelMatchRepository.save(match);

//        sending message to sender and reciver that an invite is created
        Profile senderProfile = profileRepository.findProfileById(senderId);
        Profile receiverProfile = profileRepository.findProfileById(receiverId);
        String senderName = senderProfile != null ? senderProfile.getFullName() : "Someone";
        String receiverName = receiverProfile != null ? receiverProfile.getFullName() : "Someone";

        whatsAppService.sendText(sender.getPhoneNumber(),
                "You sent an invite to " + receiverName + ".");
        whatsAppService.sendText(receiver.getPhoneNumber(),
                senderName + " sent you an invite: " + message);
    }

    // respond to an invite: only the status changes
    public void update(Integer id, Integer userId, String status) {
        TravelMatch match = travelMatchRepository.findTravelMatchById(id);
        if (match == null)
            throw new ApiException("invite not found");

        if (!match.getReceiver().getId().equals(userId))
            throw new ApiException("only the receiver can respond");

        if (!status.equals("accepted") && !status.equals("rejected"))
            throw new ApiException("status must be accepted or rejected");

        if (!match.getStatus().equals("pending"))
            throw new ApiException("this invite was already answered");

        match.setStatus(status);
        travelMatchRepository.save(match);

//        Sending whatsapp message
        Profile receiverProfile = profileRepository.findProfileById(userId);
        String receiverName = receiverProfile != null ? receiverProfile.getFullName() : "Someone";
        String senderPhone = match.getSender().getPhoneNumber();

        if (status.equals("accepted"))
            whatsAppService.sendText(senderPhone,
                    receiverName + " accepted your invite. You can chat with them on " + match.getReceiver().getPhoneNumber());
        else
            whatsAppService.sendText(senderPhone, receiverName + " declined your invite.");
    }

    // no delete: a rejected row must stay, and rows are removed by the user-delete cascade

//    Extra endpoints:


    //canceled end point
//    return the profiles of people in which they are in the same city and country the user is in.
//public List<Profile> getMatches(Integer userId) {
//    TravelPresence mine = travelPresenceRepository.findTravelPresenceById(userId);
//    if (mine == null)
//        throw new ApiException("check in to a city first");
//
//    List<Integer> ids = new ArrayList<>();
//    for (TravelPresence p : travelPresenceRepository.findByCountryAndCity(mine.getCountry(), mine.getCity())) {
//        Integer otherId = p.getId();
//        if (otherId.equals(userId))
//            continue;
//        if (blockedUserRepository.existsByBlockerIdAndBlockedId(userId, otherId)
//                || blockedUserRepository.existsByBlockerIdAndBlockedId(otherId, userId))
//            continue;
//        ids.add(otherId);
//    }
//    return profileRepository.findAllById(ids);
//}

    // invites I sent, with the receiver's profile
    public List<SentInviteDTO> getSent(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new ApiException("user not found");

        List<SentInviteDTO> result = new ArrayList<>();
        for (TravelMatch m : travelMatchRepository.findTravelMatchesBySenderId(userId)) {
            Profile receiverProfile = profileRepository.findProfileById(m.getReceiver().getId());
            result.add(new SentInviteDTO(m.getId(), m.getMessage(), m.getStatus(), m.getCreatedAt(), receiverProfile));
        }
        return result;
    }

    // invites I received, with the sender's profile
    public List<ReceivedInviteDTO> getReceived(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new ApiException("user not found");

        List<ReceivedInviteDTO> result = new ArrayList<>();
        for (TravelMatch m : travelMatchRepository.findTravelMatchesByReceiverId(userId)) {
            Profile senderProfile = profileRepository.findProfileById(m.getSender().getId());
            result.add(new ReceivedInviteDTO(m.getId(), m.getMessage(), m.getStatus(), m.getCreatedAt(), senderProfile));
        }
        return result;
    }

    // my accepted connections (the other person's profile)
    public List<Profile> getAccepted(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new ApiException("user not found");

        List<Integer> ids = new ArrayList<>();

        for (TravelMatch m : travelMatchRepository.findTravelMatchesBySenderIdAndStatus(userId, "accepted"))
            ids.add(m.getReceiver().getId());

        for (TravelMatch m : travelMatchRepository.findTravelMatchesByReceiverIdAndStatus(userId, "accepted"))
            ids.add(m.getSender().getId());

        return profileRepository.findAllById(ids);


    }

//    pending count via user id
    public Integer getPendingCount(Integer userId) {
        if (!userRepository.existsById(userId))
            throw new ApiException("user not found");

        return (int) travelMatchRepository.countByReceiverIdAndStatus(userId, "pending");
    }

    // the other user's phone number, only for an accepted invite I am part of
    public ContactDTO getContact(Integer inviteId, Integer userId) {
        TravelMatch match = travelMatchRepository.findTravelMatchById(inviteId);
        if (match == null)
            throw new ApiException("invite not found");

        if (!match.getStatus().equals("accepted"))
            throw new ApiException("the invite is not accepted yet");

        User other;
        if (match.getSender().getId().equals(userId))
            other = match.getReceiver();
        else if (match.getReceiver().getId().equals(userId))
            other = match.getSender();
        else
            throw new ApiException("you are not part of this invite");

        return new ContactDTO(other.getId(), other.getPhoneNumber());
    }

    // people in my city that I have not invited yet (excludes me and blocked users)
    public List<Profile> getNotInvited(Integer userId) {
        TravelPresence mine = travelPresenceRepository.findTravelPresenceById(userId);
        if (mine == null)
            throw new ApiException("check in to a city first");

        List<Integer> ids = new ArrayList<>();
        for (TravelPresence p : travelPresenceRepository.findByCountryAndCity(mine.getCountry(), mine.getCity())) {
            Integer otherId = p.getId();
            if (otherId.equals(userId))
                continue;
            if (blockedUserRepository.existsByBlockerIdAndBlockedId(userId, otherId)
                    || blockedUserRepository.existsByBlockerIdAndBlockedId(otherId, userId))
                continue;
            if (travelMatchRepository.existsBySenderIdAndReceiverId(userId, otherId))
                continue;
            ids.add(otherId);
        }
        return profileRepository.findAllById(ids);
    }
}
