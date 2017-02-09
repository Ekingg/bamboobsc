/* 
 * Copyright 2012-2017 bambooCORE, greenstep of copyright Chen Xin Nien
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * -----------------------------------------------------------------------
 * 
 * author: 	Chen Xin Nien
 * contact: chen.xin.nien@gmail.com
 * 
 */
package com.netsteadfast.greenstep.bsc.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.espy.arima.ArimaForecaster;
import org.espy.arima.DefaultArimaForecaster;
import org.espy.arima.DefaultArimaProcess;

import com.netsteadfast.greenstep.base.AppContext;
import com.netsteadfast.greenstep.bsc.service.ITsaMaCoefficientsService;
import com.netsteadfast.greenstep.bsc.service.ITsaService;
import com.netsteadfast.greenstep.po.hbm.BbTsa;
import com.netsteadfast.greenstep.po.hbm.BbTsaMaCoefficients;
import com.netsteadfast.greenstep.vo.TsaMaCoefficientsVO;
import com.netsteadfast.greenstep.vo.TsaVO;

@SuppressWarnings("unchecked")
public class TimeSeriesAnalysisUtils {
	
	private static ITsaService<TsaVO, BbTsa, String> tsaService;
	private static ITsaMaCoefficientsService<TsaMaCoefficientsVO, BbTsaMaCoefficients, String> tsaMaCoefficientsService;	
	
	static {
		tsaService = (ITsaService<TsaVO, BbTsa, String>) AppContext.getBean("bsc.service.TsaService");
		tsaMaCoefficientsService = (ITsaMaCoefficientsService<TsaMaCoefficientsVO, BbTsaMaCoefficients, String>) AppContext.getBean("bsc.service.TsaMaCoefficientsService");
	}
	
	public static double[] getForecastNext(TsaVO tsa, double[] observations) throws Exception {
		if (null == observations || observations.length < 1) {
			throw new IllegalArgumentException("observations array cannot not zero");
		}
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("tsaOid", tsa.getOid());
		Map<String, String> orderByParam = new HashMap<String, String>();
		orderByParam.put("seq", "ASC");
		List<BbTsaMaCoefficients> coefficients = tsaMaCoefficientsService.findListByParams(paramMap, null, orderByParam);
		if (coefficients == null || coefficients.size() != 3) {
			throw new IllegalStateException("Ma coefficients data error");
		}
		
        DefaultArimaProcess arimaProcess = new DefaultArimaProcess();
        arimaProcess.setMaCoefficients(coefficients.get(0).getSeqValue(), coefficients.get(1).getSeqValue(), coefficients.get(2).getSeqValue());
        arimaProcess.setIntegrationOrder( tsa.getIntegrationOrder() );
        
        ArimaForecaster arimaForecaster = new DefaultArimaForecaster(arimaProcess, observations);
        double[] forecast = arimaForecaster.next( tsa.getForecastNext() );
        
        return forecast;
	}
	
}