package com.sherakbar.family.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.sherakbar.family.data.model.AccountStatus
import com.sherakbar.family.data.model.Announcement
import com.sherakbar.family.data.model.AnnouncementCategory
import com.sherakbar.family.data.model.FamilyMember
import com.sherakbar.family.data.model.GalleryPhoto
import com.sherakbar.family.data.model.Gender
import com.sherakbar.family.data.model.MaritalStatus
import com.sherakbar.family.data.model.PhotoAlbum
import com.sherakbar.family.data.model.UserAccount
import com.sherakbar.family.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class Converters {
    @TypeConverter fun fromGender(v: Gender): String = v.name
    @TypeConverter fun toGender(v: String): Gender = Gender.valueOf(v)

    @TypeConverter fun fromMarital(v: MaritalStatus): String = v.name
    @TypeConverter fun toMarital(v: String): MaritalStatus = MaritalStatus.valueOf(v)

    @TypeConverter fun fromAnnouncementCategory(v: AnnouncementCategory): String = v.name
    @TypeConverter fun toAnnouncementCategory(v: String): AnnouncementCategory = AnnouncementCategory.valueOf(v)

    @TypeConverter fun fromUserRole(v: UserRole): String = v.name
    @TypeConverter fun toUserRole(v: String): UserRole = UserRole.valueOf(v)

    @TypeConverter fun fromAccountStatus(v: AccountStatus): String = v.name
    @TypeConverter fun toAccountStatus(v: String): AccountStatus = AccountStatus.valueOf(v)
}

@Database(
    entities = [
        FamilyMember::class,
        Announcement::class,
        PhotoAlbum::class,
        GalleryPhoto::class,
        UserAccount::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SherAkbarDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun galleryDao(): GalleryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: SherAkbarDatabase? = null

        fun getInstance(context: Context): SherAkbarDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SherAkbarDatabase::class.java,
                    "sher_akbar_family.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { db ->
                        INSTANCE = db
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialDataIfEmpty(db)
                        }
                    }
            }
        }

        fun hashPassword(raw: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val salted = "SherAkbarFamily::2026::$raw".toByteArray()
            return digest.digest(salted).joinToString("") { "%02x".format(it) }
        }

        suspend fun seedInitialDataIfEmpty(db: SherAkbarDatabase) {
            if (db.memberDao().getAllOnce().isNotEmpty()) return

            val now = System.currentTimeMillis()
            val day = 86_400_000L

            val members = listOf(
                // Generation 1 - Founders
                FamilyMember(
                    id = "m_sher_akbar",
                    fullName = "Haji Sher Akbar Khan",
                    fullNameUrdu = "حاجی شیر اکبر خان",
                    drawableResName = "patriarch_sher_akbar",
                    gender = Gender.MALE,
                    dateOfBirth = "1944-03-14",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_zainab",
                    spouseName = "Begum Zainab Bibi",
                    contactNumber = "+92 300 5550101",
                    address = "Sher Akbar Haveli, Peshawar Road, Swabi",
                    generation = 1,
                    biography = "Patriarch and founder of the Sher Akbar Family legacy. Known across the valley for his integrity, hospitality, community leadership, and lifelong devotion to education and family unity.",
                    biographyUrdu = "شیر اکبر خاندان کے بانی اور بزرگ۔ دیانتداری، مہمان نوازی اور تعلیم کے فروغ کے لیے جانے جاتے ہیں۔",
                    isElder = true,
                    createdAt = now - 14 * day
                ),
                FamilyMember(
                    id = "m_zainab",
                    fullName = "Begum Zainab Bibi",
                    fullNameUrdu = "بیگم زینب بی بی",
                    gender = Gender.FEMALE,
                    dateOfBirth = "1949-08-20",
                    dateOfDeath = "2021-11-05",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_sher_akbar",
                    spouseName = "Haji Sher Akbar Khan",
                    address = "Sher Akbar Haveli, Swabi",
                    generation = 1,
                    biography = "Beloved matriarch whose wisdom, grace, and prayers anchored four generations of the Sher Akbar household.",
                    biographyUrdu = "خاندان کی شفیق والدہ جن کی دعاؤں اور تربیت نے چار نسلوں کو جوڑے رکھا۔",
                    isElder = true,
                    isDeceased = true,
                    createdAt = now - 13 * day
                ),

                // Generation 2 - Sons, Daughter & Spouses
                FamilyMember(
                    id = "m_tariq",
                    fullName = "Tariq Mahmood Sher Akbar",
                    fullNameUrdu = "طارق محمود شیر اکبر",
                    fatherId = "m_sher_akbar",
                    fatherName = "Haji Sher Akbar Khan",
                    motherId = "m_zainab",
                    motherName = "Begum Zainab Bibi",
                    gender = Gender.MALE,
                    dateOfBirth = "1971-05-12",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_rubina",
                    spouseName = "Rubina Tariq",
                    contactNumber = "+92 300 5550201",
                    address = "F-8/3, Islamabad",
                    generation = 2,
                    biography = "Eldest son of Haji Sher Akbar Khan. Civil Engineer and family council coordinator overseeing annual gatherings.",
                    isElder = true,
                    createdAt = now - 12 * day
                ),
                FamilyMember(
                    id = "m_rubina",
                    fullName = "Rubina Tariq",
                    fullNameUrdu = "روبینہ طارق",
                    gender = Gender.FEMALE,
                    dateOfBirth = "1975-02-18",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_tariq",
                    spouseName = "Tariq Mahmood Sher Akbar",
                    contactNumber = "+92 300 5550202",
                    address = "F-8/3, Islamabad",
                    generation = 2,
                    biography = "Educator and organizer of the family women's heritage archive.",
                    createdAt = now - 11 * day
                ),
                FamilyMember(
                    id = "m_kareem",
                    fullName = "Dr. Kareem Ullah Sher Akbar",
                    fullNameUrdu = "ڈاکٹر کریم اللہ شیر اکبر",
                    fatherId = "m_sher_akbar",
                    fatherName = "Haji Sher Akbar Khan",
                    motherId = "m_zainab",
                    motherName = "Begum Zainab Bibi",
                    gender = Gender.MALE,
                    dateOfBirth = "1974-09-03",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_sadia",
                    spouseName = "Sadia Kareem",
                    contactNumber = "+92 321 5550204",
                    address = "Hayatabad Phase 4, Peshawar",
                    generation = 2,
                    biography = "Senior physician and advisor for the Sher Akbar Family Welfare Trust.",
                    isElder = true,
                    createdAt = now - 10 * day
                ),
                FamilyMember(
                    id = "m_sadia",
                    fullName = "Sadia Kareem",
                    fullNameUrdu = "سعدیہ کریم",
                    gender = Gender.FEMALE,
                    dateOfBirth = "1978-12-11",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_kareem",
                    spouseName = "Dr. Kareem Ullah Sher Akbar",
                    address = "Hayatabad Phase 4, Peshawar",
                    generation = 2,
                    biography = "Dedicated mother and philanthropic volunteer supporting local girls' schools.",
                    createdAt = now - 9 * day
                ),
                FamilyMember(
                    id = "m_fatima",
                    fullName = "Fatima Sher Akbar",
                    fullNameUrdu = "فاطمہ شیر اکبر",
                    fatherId = "m_sher_akbar",
                    fatherName = "Haji Sher Akbar Khan",
                    motherId = "m_zainab",
                    motherName = "Begum Zainab Bibi",
                    gender = Gender.FEMALE,
                    dateOfBirth = "1977-04-25",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseName = "Aslam Khan Afridi",
                    contactNumber = "+92 333 5550208",
                    address = "DHA Phase 5, Lahore",
                    generation = 2,
                    biography = "Professor of Urdu Literature and custodian of family poetry and historical letters.",
                    createdAt = now - 8 * day
                ),

                // Generation 3 - Grandchildren
                FamilyMember(
                    id = "m_kashif",
                    fullName = "Kashif Kareem Sher Akbar",
                    fullNameUrdu = "کاشف کریم شیر اکبر",
                    fatherId = "m_kareem",
                    fatherName = "Dr. Kareem Ullah Sher Akbar",
                    motherId = "m_sadia",
                    motherName = "Sadia Kareem",
                    gender = Gender.MALE,
                    dateOfBirth = "1998-06-15",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_hira",
                    spouseName = "Hira Kashif",
                    contactNumber = "+92 345 5550301",
                    address = "Islamabad",
                    generation = 3,
                    biography = "Software Architect and creator of the Sher Akbar Family digital archive.",
                    createdAt = now - 7 * day
                ),
                FamilyMember(
                    id = "m_hira",
                    fullName = "Hira Kashif",
                    fullNameUrdu = "حرا کاشف",
                    gender = Gender.FEMALE,
                    dateOfBirth = "2000-01-22",
                    maritalStatus = MaritalStatus.MARRIED,
                    spouseId = "m_kashif",
                    spouseName = "Kashif Kareem Sher Akbar",
                    address = "Islamabad",
                    generation = 3,
                    biography = "Interior designer specializing in modern Pakistani heritage motifs.",
                    createdAt = now - 6 * day
                ),
                FamilyMember(
                    id = "m_bilal",
                    fullName = "Bilal Tariq Sher Akbar",
                    fullNameUrdu = "بلال طارق شیر اکبر",
                    fatherId = "m_tariq",
                    fatherName = "Tariq Mahmood Sher Akbar",
                    motherId = "m_rubina",
                    motherName = "Rubina Tariq",
                    gender = Gender.MALE,
                    dateOfBirth = "1999-10-08",
                    maritalStatus = MaritalStatus.SINGLE,
                    contactNumber = "+92 301 5550305",
                    address = "F-8/3, Islamabad",
                    generation = 3,
                    biography = "Chartered accountant managing family enterprise audits.",
                    createdAt = now - 5 * day
                ),
                FamilyMember(
                    id = "m_ayesha",
                    fullName = "Ayesha Tariq Sher Akbar",
                    fullNameUrdu = "عائشہ طارق شیر اکبر",
                    fatherId = "m_tariq",
                    fatherName = "Tariq Mahmood Sher Akbar",
                    motherId = "m_rubina",
                    motherName = "Rubina Tariq",
                    gender = Gender.FEMALE,
                    dateOfBirth = "2002-07-19",
                    maritalStatus = MaritalStatus.SINGLE,
                    address = "F-8/3, Islamabad",
                    generation = 3,
                    biography = "Medical student at Khyber Medical College and family event photographer.",
                    createdAt = now - 4 * day
                ),
                FamilyMember(
                    id = "m_hamza",
                    fullName = "Hamza Kareem Sher Akbar",
                    fullNameUrdu = "حمزہ کریم شیر اکبر",
                    fatherId = "m_kareem",
                    fatherName = "Dr. Kareem Ullah Sher Akbar",
                    motherId = "m_sadia",
                    motherName = "Sadia Kareem",
                    gender = Gender.MALE,
                    dateOfBirth = "2004-03-30",
                    maritalStatus = MaritalStatus.SINGLE,
                    address = "Hayatabad, Peshawar",
                    generation = 3,
                    biography = "Aerospace engineering undergraduate and cricket enthusiast.",
                    createdAt = now - 3 * day
                ),

                // Generation 4 - Great-Grandchildren
                FamilyMember(
                    id = "m_zayan",
                    fullName = "Zayan Kashif Sher Akbar",
                    fullNameUrdu = "زیان کاشف شیر اکبر",
                    fatherId = "m_kashif",
                    fatherName = "Kashif Kareem Sher Akbar",
                    motherId = "m_hira",
                    motherName = "Hira Kashif",
                    gender = Gender.MALE,
                    dateOfBirth = "2024-09-14",
                    maritalStatus = MaritalStatus.SINGLE,
                    address = "Islamabad",
                    generation = 4,
                    biography = "Fourth-generation son bringing joy to the Sher Akbar family.",
                    isChild = true,
                    createdAt = now - 2 * day
                ),
                FamilyMember(
                    id = "m_amal",
                    fullName = "Amal Kashif Sher Akbar",
                    fullNameUrdu = "امل کاشف شیر اکبر",
                    fatherId = "m_kashif",
                    fatherName = "Kashif Kareem Sher Akbar",
                    motherId = "m_hira",
                    motherName = "Hira Kashif",
                    gender = Gender.FEMALE,
                    dateOfBirth = "2026-02-04",
                    maritalStatus = MaritalStatus.SINGLE,
                    address = "Islamabad",
                    generation = 4,
                    biography = "Youngest fourth-generation daughter of the Sher Akbar lineage.",
                    isChild = true,
                    createdAt = now - 1 * day
                )
            )
            db.memberDao().upsertAll(members)

            val announcements = listOf(
                Announcement(
                    id = "ann_1",
                    title = "Annual Sher Akbar Family Gathering 2026",
                    content = "All family branches from Swabi, Peshawar, Islamabad, and Lahore are warmly invited to the Annual Family Reunion at Sher Akbar Haveli on Sunday, December 20, 2026 at 12:30 PM. Traditional lunch and elders' duaa ceremony will follow.",
                    category = AnnouncementCategory.GATHERING,
                    authorName = "Tariq Mahmood Sher Akbar",
                    authorId = "admin_1",
                    eventDate = "2026-12-20",
                    isPinned = true,
                    createdAt = now - 1 * day
                ),
                Announcement(
                    id = "ann_2",
                    title = "Welcome Baby Amal Kashif Sher Akbar",
                    content = "Alhamdulillah! Kashif Kareem and Hira Kashif have been blessed with a baby girl, Amal Kashif Sher Akbar. May Allah grant her health, wisdom, and barakah.",
                    category = AnnouncementCategory.BIRTH,
                    authorName = "Dr. Kareem Ullah Sher Akbar",
                    authorId = "admin_1",
                    eventDate = "2026-02-04",
                    isPinned = false,
                    createdAt = now - 3 * day
                ),
                Announcement(
                    id = "ann_3",
                    title = "Bilal Tariq Engagement Ceremony",
                    content = "With the blessings of Haji Sher Akbar Khan, the engagement ceremony of Bilal Tariq Sher Akbar will be hosted in Islamabad next month. Formal invitations will be shared shortly.",
                    category = AnnouncementCategory.WEDDING,
                    authorName = "Rubina Tariq",
                    authorId = "admin_1",
                    eventDate = "2026-12-05",
                    isPinned = false,
                    createdAt = now - 5 * day
                ),
                Announcement(
                    id = "ann_4",
                    title = "Annual Duaa for Late Begum Zainab Bibi",
                    content = "Remembering our beloved matriarch Begum Zainab Bibi on her anniversary. Family members are requested to recite Surah Fatiha and join the Quran Khwani at Peshawar Road Haveli.",
                    category = AnnouncementCategory.FUNERAL,
                    authorName = "Haji Sher Akbar Khan",
                    authorId = "admin_1",
                    eventDate = "2026-11-05",
                    isPinned = false,
                    createdAt = now - 8 * day
                )
            )
            db.announcementDao().upsertAll(announcements)

            val albums = listOf(
                PhotoAlbum(
                    id = "alb_heritage",
                    name = "Founders & Heritage Portraits",
                    description = "Cherished portraits of Haji Sher Akbar Khan and family elders.",
                    eventDate = "2025-03-14",
                    coverDrawableResName = "patriarch_sher_akbar",
                    createdBy = "Tariq Mahmood Sher Akbar",
                    createdAt = now - 10 * day
                ),
                PhotoAlbum(
                    id = "alb_reunion",
                    name = "Eid & Haveli Gatherings",
                    description = "Memorable moments from Eid-ul-Fitr and annual family dinners in Swabi.",
                    eventDate = "2026-04-10",
                    coverDrawableResName = "patriarch_sher_akbar",
                    createdBy = "Ayesha Tariq Sher Akbar",
                    createdAt = now - 6 * day
                )
            )
            db.galleryDao().upsertAlbums(albums)

            val photos = listOf(
                GalleryPhoto(
                    id = "ph_1",
                    albumId = "alb_heritage",
                    drawableResName = "patriarch_sher_akbar",
                    caption = "Haji Sher Akbar Khan — Official Patriarch Portrait",
                    eventName = "Heritage Archive",
                    eventDate = "2025-03-14",
                    uploadedBy = "Kashif Kareem Sher Akbar",
                    createdAt = now - 9 * day
                ),
                GalleryPhoto(
                    id = "ph_2",
                    albumId = "alb_reunion",
                    drawableResName = "patriarch_sher_akbar",
                    caption = "Elder's Eid Blessing at Sher Akbar Haveli",
                    eventName = "Eid Reunion",
                    eventDate = "2026-04-10",
                    uploadedBy = "Ayesha Tariq Sher Akbar",
                    createdAt = now - 5 * day
                )
            )
            db.galleryDao().upsertPhotos(photos)

            val defaultUsers = listOf(
                UserAccount(
                    uid = "admin_1",
                    fullName = "Kashif Kareem (Admin)",
                    email = "admin@sherakbar.family",
                    passwordHash = hashPassword("SherAkbar@2026"),
                    role = UserRole.ADMIN,
                    status = AccountStatus.APPROVED,
                    linkedMemberId = "m_kashif",
                    createdAt = now - 15 * day
                ),
                UserAccount(
                    uid = "member_1",
                    fullName = "Bilal Tariq (Member)",
                    email = "member@sherakbar.family",
                    passwordHash = hashPassword("Family@2026"),
                    role = UserRole.MEMBER,
                    status = AccountStatus.APPROVED,
                    linkedMemberId = "m_bilal",
                    createdAt = now - 10 * day
                )
            )
            defaultUsers.forEach { db.userDao().upsert(it) }
        }
    }
}
