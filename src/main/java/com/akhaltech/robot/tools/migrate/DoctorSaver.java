package com.akhaltech.robot.tools.migrate;

import com.akhaltech.robot.common.QueryUtil;
import com.akhaltech.robot.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by vzhang on 03/03/2016.
 */
@Repository
public class DoctorSaver extends NamedParameterJdbcDaoSupport {

    @Autowired
    public DoctorSaver(DataSource dataSource) {
        super();
        setDataSource(dataSource);
    }

    @Transactional
    public void save(Doctor doctor) throws Exception {
        saveDoctor(doctor);
    }

    private void saveDoctor(final Doctor doctor) throws Exception {
        Long profileId = null;
        if(doctor.getProfile() != null) {
            profileId = saveProfile(doctor.getProfile());
        }
        final Long profileIdFinal = profileId;

        Long locationId = null;
        if(doctor.getLocation() != null) {
            locationId = saveLocation(doctor.getLocation());
        }
        final Long locationIdFinal = locationId;

        Long registrationId = null;
        if(doctor.getRegistration() != null) {
            registrationId = saveRegistration(doctor.getRegistration());
        }
        final Long registrationIdFinal = registrationId;

        final String sql = QueryUtil.getQuery("doctor", "saveDoctor");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, doctor.get_id());
                    pst.setString(2, doctor.getStatus());
                    pst.setString(3, doctor.getProvince());
                    pst.setString(4, doctor.getCountry());
                    pst.setLong(5, profileIdFinal);
                    pst.setLong(6, locationIdFinal);
                    if(registrationIdFinal != null) {
                        pst.setLong(7, registrationIdFinal);
                    }else {
                        pst.setNull(7, Types.NUMERIC);
                    }
                    return pst;
                }
            },
            keyHolder
        );

        if(doctor.getSpecialtyList() != null && doctor.getSpecialtyList().size() > 0) {
            for(Specialty specialty : doctor.getSpecialtyList()) {
                saveSpecialty(specialty, doctor.get_id());
            }
        }

        if(doctor.getPrivilegeList() != null && doctor.getPrivilegeList().size() > 0) {
            for(Privilege privilege : doctor.getPrivilegeList()) {
                savePrivilege(privilege, doctor.get_id());
            }
        }

    }

    private Long saveProfile(final Profile profile) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveProfile");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, profile.getGivenName());
                    pst.setString(2, profile.getSurname());
                    pst.setString(3, profile.getFormerName());
                    pst.setString(4, profile.getGender());
                    return pst;
                }
            },
            keyHolder
        );
        Long profileId = (Long)keyHolder.getKey();

        if(profile.getLanguageList() != null && profile.getLanguageList().size() > 0) {
            for(String language : profile.getLanguageList()) {
                saveLanguage(language, profileId);
            }
        }

        return profileId;
    }

    private void saveLanguage(final String language, final Long profileId) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveLanguage");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setLong(1, profileId);
                    pst.setString(2, language);
                    return pst;
                }
            },
            keyHolder
        );
    }

    private Long saveLocation(final Location location) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveLocation");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, location.getAddressSummary());
                    pst.setString(2, location.getContactSummary());
                    pst.setString(3, location.getElectoralDistrict());
                    pst.setString(4, location.getCorporationName());
                    pst.setString(5, location.getMedicalLicensesInOtherJurisdictions());
                    return pst;
                }
            },
            keyHolder
        );
        return (Long)keyHolder.getKey();
    }

    private Long saveRegistration(final Registration registration) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveRegistration");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setString(1, registration.getRegistrationClass());
                    pst.setString(2, registration.getCertificateIssued());
                    pst.setString(3, registration.getRegistrationStatus());
                    pst.setString(4, registration.getEffectiveFrom());
                    pst.setString(5, registration.getExpiryDate());
                    pst.setString(6, registration.getGraduatedFrom());
                    pst.setString(7, registration.getYearOfGraduation());
                    pst.setString(8, registration.getTermsAndConditions());
                    return pst;
                }
            },
            keyHolder
        );
        Long registrationId = (Long)keyHolder.getKey();

        if(registration.getHistoryList() != null && registration.getHistoryList().size() > 0) {
            for(RegistrationHistory history : registration.getHistoryList()) {
                saveRegistrationHistory(history, registrationId);
            }
        }

        if(registration.getTrainingList() != null && registration.getTrainingList().size() > 0) {
            for(PostgraduateTraining training : registration.getTrainingList()) {
                savePostgraduateTraining(training, registrationId);
            }
        }

        return registrationId;
    }

    private void saveRegistrationHistory(final RegistrationHistory history, final Long registrationId) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveRegistrationHistory");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setLong(1, registrationId);
                    pst.setString(2, history.getDescription());
                    pst.setString(3, history.getEffectiveDate());
                    return pst;
                }
            },
            keyHolder
        );
    }

    private void savePostgraduateTraining(final PostgraduateTraining training, final Long registrationId) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "savePostgraduateTraining");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
            new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    pst.setLong(1, registrationId);
                    pst.setString(2, training.getType());
                    pst.setString(3, training.getDiscipline());
                    pst.setString(4, training.getMedicalSchool());
                    pst.setString(5, training.getFrom());
                    pst.setString(6, training.getTo());
                    return pst;
                }
            },
            keyHolder
        );
    }

    private void savePrivilege(final Privilege privilege, final String doctorId) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "savePrivilege");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        pst.setString(1, doctorId);
                        pst.setString(2, privilege.getHospitalDetail());
                        return pst;
                    }
                },
                keyHolder
        );
    }

    private void saveSpecialty(final Specialty specialty, final String doctorId) throws Exception {
        final String sql = QueryUtil.getQuery("doctor", "saveSpecialty");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        pst.setString(1, doctorId);
                        pst.setString(2, specialty.getName());
                        pst.setString(3, specialty.getIssueOn());
                        pst.setString(4, specialty.getType());
                        return pst;
                    }
                },
                keyHolder
        );
    }
}
