package com.qi_backend.repository;

import com.qi_backend.entity.MediaFile;
import com.qi_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    List<MediaFile> findByUploaderOrderByUploadedAtDesc(User uploader);
}
