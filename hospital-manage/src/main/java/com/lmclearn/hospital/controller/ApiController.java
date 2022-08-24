package com.lmclearn.hospital.controller;

import com.lmclearn.hospital.mapper.HospitalSetMapper;
import com.lmclearn.hospital.model.HospitalSet;
import com.lmclearn.hospital.service.ApiService;
import com.lmclearn.hospital.util.YyghException;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author qy
 *
 */
@Api(tags = "医院管理接口")
@Controller
@RequestMapping
public class ApiController extends BaseController {

	@Autowired
	private ApiService apiService;

	@Autowired
	private HospitalSetMapper hospitalSetMapper;

	@RequestMapping("/hospitalSet/index")
	public String getHospitalSet(ModelMap model,RedirectAttributes redirectAttributes) {
		HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
		model.addAttribute("hospitalSet", hospitalSet);
		return "hospitalSet/index";
	}

	@RequestMapping(value="/hospitalSet/save")
	public String createHospitalSet(ModelMap model,HospitalSet hospitalSet) {
		hospitalSetMapper.updateById(hospitalSet);
		return "redirect:/hospitalSet/index";
	}

	/**
	 * 查询医院信息
	 * @param model
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("/hospital/index")
	public String getHospital(ModelMap model,HttpServletRequest request,RedirectAttributes redirectAttributes) {
		try {
			HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
			if(null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
				this.failureMessage("先设置医院code与签名key", redirectAttributes);
				return "redirect:/hospitalSet/index";
			}

			model.addAttribute("hospital", apiService.getHospital());
		} catch (YyghException e) {
			this.failureMessage(e.getMessage(), request);
		} catch (Exception e) {
			this.failureMessage("数据异常", request);
		}
		return "hospital/index";
	}

	@RequestMapping(value="/hospital/create")
	public String createHospital(ModelMap model) {
		return "hospital/create";
	}

	/**
	 * 上传医院
	 * @param data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/hospital/save",method=RequestMethod.POST)
	public String saveHospital(String data, HttpServletRequest request) {
		try {
			apiService.saveHospital(data);
		} catch (YyghException e) {
			return this.failurePage(e.getMessage(),request);
		} catch (Exception e) {
			return this.failurePage("数据异常",request);
		}
		return this.successPage(null,request);
	}

	/**
	 * 科室信息查询
	 * @param model
	 * @param pageNum
	 * @param pageSize
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("/department/list")
	public String findDepartment(ModelMap model,
								 @RequestParam(defaultValue = "1") int pageNum,
								 @RequestParam(defaultValue = "10") int pageSize,
								 HttpServletRequest request,RedirectAttributes redirectAttributes) {
		try {
			HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
			if(null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
				this.failureMessage("先设置医院code与签名key", redirectAttributes);
				return "redirect:/hospitalSet/index";
			}

			model.addAllAttributes(apiService.findDepartment(pageNum, pageSize));
		} catch (YyghException e) {
			this.failureMessage(e.getMessage(), request);
		} catch (Exception e) {
			this.failureMessage("数据异常", request);
		}
		return "department/index";
	}

	@RequestMapping(value="/department/create")
	public String create(ModelMap model) {
		return "department/create";
	}

	/**
	 * 科室上传
	 * @param data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/department/save",method=RequestMethod.POST)
	public String save(String data, HttpServletRequest request) {
		try {
			apiService.saveDepartment(data);
		} catch (YyghException e) {
			return this.failurePage(e.getMessage(),request);
		} catch (Exception e) {
			return this.failurePage("数据异常",request);
		}
		return this.successPage(null,request);
	}

	/**
	 * 排班信息查询
	 * @param model
	 * @param pageNum 页码
	 * @param pageSize 每页的信息条数
	 * @param request
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("/schedule/list")
	public String findSchedule(ModelMap model,
								 @RequestParam(defaultValue = "1") int pageNum,
								 @RequestParam(defaultValue = "10") int pageSize,
							   HttpServletRequest request,RedirectAttributes redirectAttributes) {
		try {
			HospitalSet hospitalSet = hospitalSetMapper.selectById(1);
			if(null == hospitalSet || StringUtils.isEmpty(hospitalSet.getHoscode()) || StringUtils.isEmpty(hospitalSet.getSignKey())) {
				this.failureMessage("先设置医院code与签名key", redirectAttributes);
				return "redirect:/hospitalSet/index";
			}

			model.addAllAttributes(apiService.findSchedule(pageNum, pageSize));
		} catch (YyghException e) {
			this.failureMessage(e.getMessage(), request);
		} catch (Exception e) {
			this.failureMessage("数据异常", request);
		}
		return "schedule/index";
	}

	@RequestMapping(value="/schedule/create")
	public String createSchedule(ModelMap model) {
		return "schedule/create";
	}

	/**
	 * 上传排班
	 * @param data
	 * @param request
	 * @return
	 */
	@RequestMapping(value="/schedule/save",method=RequestMethod.POST)
	public String saveSchedule(String data, HttpServletRequest request) {
		try {
			//data = data.replaceAll("\r\n", "").replace(" ", "");
			apiService.saveSchedule(data);
		} catch (YyghException e) {
			return this.failurePage(e.getMessage(),request);
		} catch (Exception e) {
			e.printStackTrace();
			return this.failurePage("数据异常："+e.getMessage(),request);
		}
		return this.successPage(null,request);
	}

	@RequestMapping(value="/hospital/createBatch")
	public String createHospitalBatch(ModelMap model) {
		return "hospital/createBatch";
	}

	@RequestMapping(value="/hospital/saveBatch",method=RequestMethod.POST)
	public String saveBatchHospital(HttpServletRequest request) {
		try {
			apiService.saveBatchHospital();
		} catch (YyghException e) {
			return this.failurePage(e.getMessage(),request);
		} catch (Exception e) {
			return this.failurePage("数据异常",request);
		}
		return this.successPage(null,request);
	}

	/**
	 * 删除科室信息
	 * @param model
	 * @param depcode 科室编号
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value="/department/remove/{depcode}",method=RequestMethod.GET)
	public String removeDepartment(ModelMap model, @PathVariable String depcode, RedirectAttributes redirectAttributes) {
		apiService.removeDepartment(depcode);
		this.successMessage(null, redirectAttributes);
		return "redirect:/department/list";
	}

	/**
	 * 排班信息删除
	 * @param model
	 * @param hosScheduleId
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping(value="/schedule/remove/{hosScheduleId}",method=RequestMethod.GET)
	public String removeSchedule(ModelMap model, @PathVariable String hosScheduleId, RedirectAttributes redirectAttributes) {
		apiService.removeSchedule(hosScheduleId);

		this.successMessage(null, redirectAttributes);
		return "redirect:/schedule/list";
	}

}

