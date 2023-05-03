package Capstone.Crewpass.service;

import Capstone.Crewpass.dto.RecruitmentDeadlineList;
import Capstone.Crewpass.dto.RecruitmentDetail;
import Capstone.Crewpass.dto.RecruitmentRecentList;
import Capstone.Crewpass.entity.Recruitment;
import Capstone.Crewpass.repository.RecruitmentRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;

    public RecruitmentService(RecruitmentRepository recruitmentRepository) {
        this.recruitmentRepository = recruitmentRepository;
    }

    // 사진 파일 업로드
    public String uploadImage(MultipartFile image) throws IOException {
        //GCP에 저장할 파일의 이름 생성
        String fileName = UUID.randomUUID().toString().concat(image.getOriginalFilename());

        //GCP에 파일 upload
        ClassPathResource resource = new ClassPathResource("capstone-design-385100-a6607b6659e7.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        String projectId = "capstone-design-385100";
        Storage storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();

        storage.create(
                BlobInfo.newBuilder("crewpass-cloud-storage", fileName)
                        .build(),
                image.getInputStream()
        );

        // 서버에 등록한 파일명을 반환
        return fileName;
    }

    // 모집글 등록
    public String registerRecruitment(Recruitment recruitment) {
        if (validateDuplicateRecruitment(recruitment) != null) {
            recruitmentRepository.save(recruitment);
            return "registerRecruitment - success" + " : RecruitmentId = " + recruitment.getRecruitmentId();
        } else {
            return null;
        }
    }

    // 중복 모집글 검증
    private String validateDuplicateRecruitment(Recruitment recruitment) {
        Optional<Recruitment> optionalRecruitment = recruitmentRepository.findByRecruitmentId(recruitment.getRecruitmentId());
        if (optionalRecruitment.isPresent()) {
            return null;
        } else {
            return "validateDuplicateRecruitment - success";
        }
    }

    // 로그인한 동아리가 작성한 모집글 목록 조회
    public List<RecruitmentRecentList> checkMyRecruitmentList(Integer crewId) {
        return recruitmentRepository.findMyRecruitmentList(crewId);
    }

    // 동아리 분야 별 "최신순"으로 모집글 목록 조회
    public List<RecruitmentRecentList> checkRecruitmentListByNewest(String field) {

        if (field.equals("total")) { // 전체 분야인 경우
            return recruitmentRepository.findAllRecruitmentListByNewest();
        }

        // 분야가 선택된 경우
        return recruitmentRepository.findFieldRecruitmentListByNewest(field);
    }

    // 동아리 분야 별 "마감임박순"으로 모집글 목록 조회
    public List<RecruitmentDeadlineList> checkRecruitmentListByDeadline(String field) {

        if (field.equals("total")) { // 전체 분야인 경우
            return recruitmentRepository.findAllRecruitmentListByDeadline();
        }

        // 분야가 선택된 경우
        return recruitmentRepository.findFieldRecruitmentListByDeadline(field);
    }

    // 선택한 모집글 상세 조회
    public List<RecruitmentDetail> checkRecruitmentDetail(Integer recruitmentId) {

        return recruitmentRepository.getRecruitmentDetail(recruitmentId);
    }
}