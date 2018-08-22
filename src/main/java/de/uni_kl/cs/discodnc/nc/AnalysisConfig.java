/*
 * This file is part of the Disco Deterministic Network Calculator.
 *
 * Copyright (C) 2013 - 2018 Steffen Bondorf
 * Copyright (C) 2017+ The DiscoDNC contributors
 *
 * Distributed Computer Systems (DISCO) Lab
 * University of Kaiserslautern, Germany
 *
 * http://discodnc.cs.uni-kl.de
 *
 *
 * The Disco Deterministic Network Calculator (DiscoDNC) is free software;
 * you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation; 
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package de.uni_kl.cs.discodnc.nc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AnalysisConfig {
    public enum Multiplexing {
        ARBITRARY, FIFO
    }

    public enum MuxDiscipline {
        SERVER_LOCAL, GLOBAL_ARBITRARY, GLOBAL_FIFO
    }

    public enum GammaFlag {
        SERVER_LOCAL, GLOBALLY_ON, GLOBALLY_OFF
    }

    public enum ArrivalBoundMethod {
		AGGR_PBOO_PER_SERVER, AGGR_PBOO_CONCATENATION, AGGR_PMOO, AGGR_TM,
		SEGR_PBOO, SEGR_PMOO, SEGR_TM, 
		SINKTREE_AFFINE, SINKTREE_AFFINE_CONV, SINKTREE_AFFINE_CONV_DECONV, SINKTREE_AFFINE_HOMO
    }
    
    private MuxDiscipline multiplexing_discipline = MuxDiscipline.SERVER_LOCAL;
    /**
     * Whether to use maximum service curves in output bound computation
     */
    private GammaFlag use_gamma = GammaFlag.SERVER_LOCAL;
    /**
     * Whether to constrain the output bound further through convolution with the
     * maximum service curve's rate as the server cannot output data faster than
     * this rate.
     */
    private GammaFlag use_extra_gamma = GammaFlag.SERVER_LOCAL;
    private Set<ArrivalBoundMethod> arrival_bound_methods = new HashSet<ArrivalBoundMethod>(
            Collections.singleton(ArrivalBoundMethod.AGGR_PBOO_CONCATENATION));
    private boolean remove_duplicate_arrival_bounds = true;
    private boolean flow_prolongation = false;
    private boolean server_backlog_arrival_bound = false;
    
    public AnalysisConfig() {
    }
    
    public AnalysisConfig(MuxDiscipline multiplexing_discipline, GammaFlag use_gamma, GammaFlag use_extra_gamma,
                          Set<ArrivalBoundMethod> arrival_bound_methods, boolean remove_duplicate_arrival_bounds,
                          boolean server_backlog_arrival_bound) {
        this.multiplexing_discipline = multiplexing_discipline;
        this.use_gamma = use_gamma;
        this.use_extra_gamma = use_extra_gamma;
        this.arrival_bound_methods.clear();
        this.arrival_bound_methods.addAll(arrival_bound_methods);
        this.remove_duplicate_arrival_bounds = remove_duplicate_arrival_bounds;
        this.server_backlog_arrival_bound = server_backlog_arrival_bound;
    }

    public MuxDiscipline multiplexingDiscipline() {
        return multiplexing_discipline;
    }

    public void setMultiplexingDiscipline(MuxDiscipline mux_discipline) {
        multiplexing_discipline = mux_discipline;
    }

    public GammaFlag useGamma() {
        return use_gamma;
    }

    public void setUseGamma(GammaFlag use_gamma_flag) {
        use_gamma = use_gamma_flag;
    }

    public GammaFlag useExtraGamma() {
        return use_extra_gamma;
    }

    public void setUseExtraGamma(GammaFlag use_extra_gamma_flag) {
        use_extra_gamma = use_extra_gamma_flag;
    }

    public void defaultArrivalBoundMethods() {
        clearArrivalBoundMethods();
        arrival_bound_methods.add(ArrivalBoundMethod.AGGR_PBOO_CONCATENATION);
    }

    public void clearArrivalBoundMethods() {
        arrival_bound_methods.clear();
    }

    public void setArrivalBoundMethod(ArrivalBoundMethod arrival_bound_method) {
        clearArrivalBoundMethods();
        arrival_bound_methods.add(arrival_bound_method);
    }

    public Set<ArrivalBoundMethod> arrivalBoundMethods() {
        return new HashSet<ArrivalBoundMethod>(arrival_bound_methods);
    }

    public void setArrivalBoundMethods(Set<ArrivalBoundMethod> arrival_bound_methods_set) {
        clearArrivalBoundMethods();
        arrival_bound_methods.addAll(arrival_bound_methods_set);
    }

    public void addArrivalBoundMethod(ArrivalBoundMethod arrival_bound_method) {
        arrival_bound_methods.add(arrival_bound_method);
    }

    public void addArrivalBoundMethods(Set<ArrivalBoundMethod> arrival_bound_methods_set) {
        arrival_bound_methods.addAll(arrival_bound_methods_set);
    }

    public boolean removeArrivalBoundMethod(ArrivalBoundMethod arrival_bound_method) {
        if (arrival_bound_methods.size() == 1) {
            return false;
        } else {
            return arrival_bound_methods.remove(arrival_bound_method);
        }
    }

    public boolean removeDuplicateArrivalBounds() {
        return remove_duplicate_arrival_bounds;
    }

    public void setRemoveDuplicateArrivalBounds(boolean remove_duplicate_arrival_bounds_flag) {
        remove_duplicate_arrival_bounds = remove_duplicate_arrival_bounds_flag;
    }

    public boolean serverBacklogArrivalBound() {
        return server_backlog_arrival_bound;
    }

    public void setServerBacklogArrivalBound(boolean server_backlog_arrival_bound) {
        this.server_backlog_arrival_bound = server_backlog_arrival_bound;
    }

    public boolean useFlowProlongation() {
        return flow_prolongation;
    }

    public void setUseFlowProlongation(boolean prolong_flows) {
        flow_prolongation = prolong_flows;
    }

    /**
     * Returns a deep copy of this analysis configuration.
     *
     * @return The copy.
     */
    public AnalysisConfig copy() { // deep copy as primitive data types are copied by value
        return new AnalysisConfig(multiplexing_discipline, use_gamma, use_extra_gamma, arrival_bound_methods,
                remove_duplicate_arrival_bounds, server_backlog_arrival_bound);
    }

    @Override
    public String toString() {
        StringBuffer analysis_config_str = new StringBuffer();

        analysis_config_str.append(multiplexingDiscipline().toString());
        analysis_config_str.append(", ");
        analysis_config_str.append(arrivalBoundMethods().toString());

        if (removeDuplicateArrivalBounds()) {
            analysis_config_str.append(", ");
            analysis_config_str.append("remove duplicate ABs");
        }

        return analysis_config_str.toString();
    }
}
