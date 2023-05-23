package Capstone.Crewpass.repository;

import Capstone.Crewpass.entity.DB.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    ChatRoom findByRecruitmentId(Integer recruitmentId);
}
