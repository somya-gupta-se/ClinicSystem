package com.clinic.system.util;

import com.clinic.system.entity.Doctor;
import com.clinic.system.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DoctorRepository doctorRepository;

    @Override
    public void run(String... args) throws Exception {
        if (doctorRepository.count() == 0) {
            log.info("Initializing sample doctors...");
            List<Doctor> doctors = Arrays.asList(
                    Doctor.builder()
                            .nameEnglish("Dr. Ahmed Hassan")
                            .nameArabic("د. أحمد حسن")
                            .specialty("Cardiology")
                            .yearsOfExperience(15)
                            .consultationDuration(30)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Fatima Al-Rashid")
                            .nameArabic("د. فاطمة الراشد")
                            .specialty("Pediatrics")
                            .yearsOfExperience(12)
                            .consultationDuration(25)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Mohammed Al-Dosari")
                            .nameArabic("د. محمد الدوسري")
                            .specialty("Orthopedics")
                            .yearsOfExperience(18)
                            .consultationDuration(45)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Layla Al-Qahtani")
                            .nameArabic("د. ليلى القحطاني")
                            .specialty("Dermatology")
                            .yearsOfExperience(10)
                            .consultationDuration(20)
                            .build(),
                    Doctor.builder()
                            .nameEnglish("Dr. Samir Al-Otaibi")
                            .nameArabic("د. سمير العتيبي")
                            .specialty("Psychiatry")
                            .yearsOfExperience(8)
                            .consultationDuration(15)
                            .build(),
                    Doctor.builder().nameEnglish("Dr. Sarah Khan").nameArabic("د. سارة خان").specialty("Dermatology").yearsOfExperience(10).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Omar Farooq").nameArabic("د. عمر فاروق").specialty("Orthopedics").yearsOfExperience(12).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Fatima Noor").nameArabic("د. فاطمة نور").specialty("Pediatrics").yearsOfExperience(8).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Ali Raza").nameArabic("د. علي رضا").specialty("Neurology").yearsOfExperience(14).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Layla Ibrahim").nameArabic("د. ليلى إبراهيم").specialty("Gynecology").yearsOfExperience(11).consultationDuration(25).build(),

                    Doctor.builder().nameEnglish("Dr. Hassan Sheikh").nameArabic("د. حسن شيخ").specialty("Cardiology").yearsOfExperience(16).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Aisha Siddiqui").nameArabic("د. عائشة صديقي").specialty("ENT").yearsOfExperience(9).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Bilal Qureshi").nameArabic("د. بلال قريشي").specialty("Urology").yearsOfExperience(13).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Zainab Ali").nameArabic("د. زينب علي").specialty("Ophthalmology").yearsOfExperience(7).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Imran Malik").nameArabic("د. عمران مالك").specialty("Radiology").yearsOfExperience(15).consultationDuration(30).build(),

                    Doctor.builder().nameEnglish("Dr. Noor Ahmed").nameArabic("د. نور أحمد").specialty("General Medicine").yearsOfExperience(6).consultationDuration(15).build(),
                    Doctor.builder().nameEnglish("Dr. Yasmin Tariq").nameArabic("د. ياسمين طارق").specialty("Dermatology").yearsOfExperience(9).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Khalid Ansari").nameArabic("د. خالد أنصاري").specialty("Orthopedics").yearsOfExperience(18).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Samina Sheikh").nameArabic("د. سمينة شيخ").specialty("Gynecology").yearsOfExperience(14).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Tariq Mehmood").nameArabic("د. طارق محمود").specialty("Neurology").yearsOfExperience(17).consultationDuration(30).build(),

                    Doctor.builder().nameEnglish("Dr. Hina Rashid").nameArabic("د. هنا رشيد").specialty("Pediatrics").yearsOfExperience(10).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Adnan Siddiq").nameArabic("د. عدنان صديق").specialty("Cardiology").yearsOfExperience(19).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Rabia Khan").nameArabic("د. رابية خان").specialty("ENT").yearsOfExperience(8).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Farhan Iqbal").nameArabic("د. فرحان إقبال").specialty("Urology").yearsOfExperience(11).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Sana Javed").nameArabic("د. سناء جاويد").specialty("Dermatology").yearsOfExperience(7).consultationDuration(20).build(),

                    Doctor.builder().nameEnglish("Dr. Waqar Ahmed").nameArabic("د. وقار أحمد").specialty("Radiology").yearsOfExperience(13).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Mehwish Ali").nameArabic("د. مهوش علي").specialty("Gynecology").yearsOfExperience(12).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Noman Raza").nameArabic("د. نعمان رضا").specialty("Orthopedics").yearsOfExperience(10).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Bushra Noor").nameArabic("د. بشرى نور").specialty("Pediatrics").yearsOfExperience(9).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Salman Haider").nameArabic("د. سلمان حيدر").specialty("Cardiology").yearsOfExperience(15).consultationDuration(30).build(),

                    Doctor.builder().nameEnglish("Dr. Adeel Khan").nameArabic("د. عادل خان").specialty("Neurology").yearsOfExperience(16).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Shazia Malik").nameArabic("د. شازية مالك").specialty("Dermatology").yearsOfExperience(11).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Hamza Tariq").nameArabic("د. حمزة طارق").specialty("General Medicine").yearsOfExperience(5).consultationDuration(15).build(),
                    Doctor.builder().nameEnglish("Dr. Nadia Sheikh").nameArabic("د. نادية شيخ").specialty("ENT").yearsOfExperience(8).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Fahad Iqbal").nameArabic("د. فهد إقبال").specialty("Urology").yearsOfExperience(12).consultationDuration(25).build(),

                    Doctor.builder().nameEnglish("Dr. Kiran Abbas").nameArabic("د. كيران عباس").specialty("Ophthalmology").yearsOfExperience(9).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Junaid Ahmed").nameArabic("د. جنيد أحمد").specialty("Radiology").yearsOfExperience(14).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Asma Khalid").nameArabic("د. أسماء خالد").specialty("Gynecology").yearsOfExperience(13).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Danish Raza").nameArabic("د. دانش رضا").specialty("Orthopedics").yearsOfExperience(17).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Saba Noor").nameArabic("د. صبا نور").specialty("Pediatrics").yearsOfExperience(6).consultationDuration(20).build(),

                    Doctor.builder().nameEnglish("Dr. Usman Ali").nameArabic("د. عثمان علي").specialty("Cardiology").yearsOfExperience(18).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Mahira Khan").nameArabic("د. مهيرة خان").specialty("Dermatology").yearsOfExperience(10).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Rizwan Sheikh").nameArabic("د. رضوان شيخ").specialty("ENT").yearsOfExperience(12).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Hammad Tariq").nameArabic("د. حماد طارق").specialty("Neurology").yearsOfExperience(15).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Komal Ahmed").nameArabic("د. كمال أحمد").specialty("Gynecology").yearsOfExperience(9).consultationDuration(25).build(),

                    Doctor.builder().nameEnglish("Dr. Arslan Khan").nameArabic("د. أرسلان خان").specialty("Orthopedics").yearsOfExperience(11).consultationDuration(25).build(),
                    Doctor.builder().nameEnglish("Dr. Iqra Noor").nameArabic("د. إقرا نور").specialty("Pediatrics").yearsOfExperience(7).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Talha Mehmood").nameArabic("د. طلحة محمود").specialty("Radiology").yearsOfExperience(13).consultationDuration(30).build(),
                    Doctor.builder().nameEnglish("Dr. Amina Siddiqui").nameArabic("د. أمينة صديقي").specialty("Dermatology").yearsOfExperience(10).consultationDuration(20).build(),
                    Doctor.builder().nameEnglish("Dr. Saad Farooq").nameArabic("د. سعد فاروق").specialty("Cardiology").yearsOfExperience(16).consultationDuration(30).build()
            );

            doctorRepository.saveAll(doctors);
            log.info("Sample doctors initialized successfully");
        }
    }
}

