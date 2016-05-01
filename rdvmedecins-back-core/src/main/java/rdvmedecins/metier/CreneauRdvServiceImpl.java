package rdvmedecins.metier;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import rdvmedecins.domain.AgendaMedecinJour;
import rdvmedecins.domain.CreneauMedecinJour;
import rdvmedecins.entities.Client;
import rdvmedecins.entities.Creneau;
import rdvmedecins.entities.Medecin;
import rdvmedecins.entities.Rv;
import rdvmedecins.repositories.CreneauRepository;
import rdvmedecins.repositories.MedecinRepository;
import rdvmedecins.repositories.RvRepository;

@Service
@Transactional
public class CreneauRdvServiceImpl implements CreneauRdvService {

	private final Logger logger = LoggerFactory.getLogger(CreneauRdvServiceImpl.class);

	
	/*
	 * DEPENDENCY INJECTION
	 * =========================================================================
	 */
	
	@Autowired
	private MedecinRepository medecinRepository;
	
	@Autowired
	private RvRepository rvRepository;

	@Autowired
	private CreneauRepository creneauRepository;
	
	
	/*
	 * APPOINTMENT SERVICE METHODS IMPLEMENTATION
	 * =========================================================================
	 */
	
	@Override
	public Rv createRv(Date jour, Creneau créneau, Client client) {
		return rvRepository.save(new Rv(jour, client, créneau));
	}

	@Override
	public Rv updateRv(Rv rv) {
		return rvRepository.save(rv);
	}

	@Override
	public void deleteRv(Long idRv) {
		rvRepository.delete(idRv);
	}

	@Override
	@Transactional(readOnly = true)
	public Rv findRvById(Long idRv) {
		return rvRepository.findOne(idRv);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Rv> findAppointmentsByDoctorByDay(long idDcotor, Date day) {
		return rvRepository.findAppointmentByDoctorByDay(idDcotor, day);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Long countAllAppointments() {
		// TODO add condition > current day, active appointment
		return rvRepository.count();
	}
	
	/*
	 * TIMESLOT SERVICE METHODS IMPLEMENTATION
	 * =========================================================================
	 */
	
	@Override
	public Creneau createTimeslot(Date jour, Creneau créneau, Client client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Creneau updateTimeslot(Creneau timeslot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteTimeslot(Long id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transactional(readOnly = true)
	public Creneau findTimeslotById(Long id) {
		return creneauRepository.findOne(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Creneau> findAllTimeslotOfDoctor(long idDoctor) {
		return creneauRepository.findAllTimeslotbyDoctor(idDoctor);
	}
	
	
	/*
	 * COMMON BUSINESS METHODS IMPLEMENTATION
	 * =========================================================================
	 */
	
	@Transactional(readOnly = true)
	public AgendaMedecinJour getAgendaMedecinJour(long idDoctor, Date day) {
		
		// get doctor's timeslot list
		List<Creneau> creneauxHoraires = findAllTimeslotOfDoctor(idDoctor);
		// get appointment list fo given doctor in given day
		List<Rv> reservations = findAppointmentsByDoctorByDay(idDoctor, day);
		
		// on crée un dictionnaire à partir des Rv pris
		Map<Long, Rv> hReservations = new Hashtable<Long, Rv>();
		for (Rv resa : reservations) {
			hReservations.put(resa.getCreneau().getId(), resa);
		}
		// on crée l'agenda pour le jour demandé
		AgendaMedecinJour agenda = new AgendaMedecinJour();
		
		// le médecin
		Medecin doctor = medecinRepository.findOne(idDoctor);
		agenda.setMedecin(doctor);		
		// le jour
		agenda.setJour(day);
		
		// les créneaux de réservation
		CreneauMedecinJour[] creneauxMedecinJour = new CreneauMedecinJour[creneauxHoraires.size()];
		agenda.setCreneauxMedecinJour(creneauxMedecinJour);
		// remplissage des créneaux de réservation
		for (int i = 0; i < creneauxHoraires.size(); i++) {
			// ligne i agenda
			creneauxMedecinJour[i] = new CreneauMedecinJour();
			// créneau horaire
			Creneau créneau = creneauxHoraires.get(i);
			long idCreneau = créneau.getId();
			creneauxMedecinJour[i].setCreneau(créneau);
			// le créneau est-il libre ou réservé ?
			if (hReservations.containsKey(idCreneau)) {
				// le créneau est occupé - on note la résa
				Rv resa = hReservations.get(idCreneau);
				creneauxMedecinJour[i].setRv(resa);
			}
		}
		// on rend le résultat
		return agenda;
	}





}
