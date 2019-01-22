package io.narayana.demo.lra.devconf2019;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import io.narayana.demo.lra.devconf2019.jpa.Flight;

@Dependent
public class FlightManagement {
    @PersistenceContext(unitName = "FlightDS")
    private EntityManager em;

    @Transactional
    public void save(Flight flight) {
        em.persist(flight);
    }

    @Transactional
    public void delete(Flight flight) {
        em.remove(flight);
    }
    
    public Flight find(int id) {
        return em.find(Flight.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Flight> getAllFlights() {
        return em.createNamedQuery("Flight.findAll").getResultList();
    }
}
