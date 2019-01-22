package io.narayana.demo.lra.devconf2019;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import io.narayana.demo.lra.devconf2019.jpa.Flight;

@Dependent
@Transactional
public class FlightManager {
    @PersistenceContext
    private EntityManager em;

    public void save(Flight flight) {
        em.persist(flight);
    }

    public void delete(Flight flight) {
        flight = em.merge(flight);
        em.remove(flight);
    }
    
    public Flight find(int id) {
        return em.find(Flight.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Flight> getAllFlights() {
        return em.createNamedQuery("Flight.findAll").getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Flight> findByDate(Date date) {
        return em.createNamedQuery("Flight.findByDate")
            .setParameter("date", date)
            .getResultList();
    }
}
