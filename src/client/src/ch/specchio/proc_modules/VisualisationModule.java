package ch.specchio.proc_modules;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ch.specchio.client.SPECCHIOClient;
import ch.specchio.client.SPECCHIOClientException;
import ch.specchio.constants.SpaceTypes;
import ch.specchio.explorers.SingleHemisphereExplorer;
import ch.specchio.explorers.SpectralMultiPlot;
import ch.specchio.explorers.TimeLineExplorer;
import ch.specchio.gui.ProgressReport;
import ch.specchio.plots.swing.SamplingPoints2DPlot;
import ch.specchio.plots.swing.TimelinePlot;
import ch.specchio.spaces.MeasurementUnit;
import ch.specchio.spaces.SpectralSpace;
import ch.specchio.types.MatlabAdaptedArrayList;

public class VisualisationModule extends Module implements ModuleCallback {

	JPanel vis_panel;
	String vis_module_type = "No module selected yet.";
	
	
	public VisualisationModule(Frame owner, SPECCHIOClient specchio_client) {
		super(owner, specchio_client);
		// TODO Auto-generated constructor stub
	}
	


	public MeasurementUnit get_measurement_unit() {
		return ((SpectralSpace)get_main_input_space().getSpace()).getMeasurementUnit();
	}

	public double[] get_output_space_vector() {
		// Output space is identical to input space		
		return ((SpectralSpace)get_main_input_space().getSpace()).getAverageWavelengths();
	}

	public int get_space_type() 
	{
		// same as input space
		return this.get_main_input_space().getSpace().getSpaceType();
	}
	
	public JPanel get_info_panel()
	{
		module_type_label = new JLabel("Vis. module:");
		module_name_label = new JLabel(vis_module_type);
		this.settings = new JLabel("");
		
		super.get_info_panel();

		return info_panel;
		
	}

	
	// input space is used for the visualisation
	// then the transformation is just copying from input to output space 
	public void transform() throws SPECCHIOClientException 
	{
		ProgressReport pr = new ProgressReport(vis_module_type, true);
		pr.setVisible(true);
		
		// update progress bar
		this.progressBar.setString("Opening " + vis_module_type);
		set_progress(50);
		
		
		if(this.vis_module_type.equals(VisualisationSelectionDialog.gonio_hem_expl))
		{
			vis_panel = new SingleHemisphereExplorer((SpectralSpace)this.get_main_input_space().getSpace(), pr, this.specchio_client);
		}
		
		if(this.vis_module_type.equals(VisualisationSelectionDialog.time_line_plot))
		{
			SpectralSpace ss = (SpectralSpace)this.get_main_input_space().getSpace();
			MatlabAdaptedArrayList<Object> time_vector = specchio_client.getMetaparameterValues(ss.getSpectrumIds(), "Acquisition Time");			
			vis_panel = new TimelinePlot(ss, time_vector, 400, 400, pr);
		}
		
		if(this.vis_module_type.equals(VisualisationSelectionDialog.time_line_expl))
		{
			vis_panel = new TimeLineExplorer(specchio_client, (SpectralSpace)this.get_main_input_space().getSpace(), pr);
		}
		
		if(this.vis_module_type.equals(VisualisationSelectionDialog.sampling_points_plot))
		{
			vis_panel = new SamplingPoints2DPlot((SpectralSpace)this.get_main_input_space().getSpace(), 400, 400, this.specchio_client);
		}

		if(this.vis_module_type.equals(VisualisationSelectionDialog.spectral_multiplot))
		{
			vis_panel = new SpectralMultiPlot((SpectralSpace)this.get_main_input_space().getSpace(), pr, 0);			
		}		
		
		if(this.vis_module_type.equals(VisualisationSelectionDialog.spectral_scatter_multiplot))
		{
			vis_panel = new SpectralMultiPlot((SpectralSpace)this.get_main_input_space().getSpace(), pr, 1);			
		}		
		
		set_progress(100);

		
		// open new dialog with the visualisation panel added
		if(vis_panel != null)
		{
			JFrame frame = new JFrame(vis_module_type + " (Space no " + Integer.toString(get_main_input_space().getNumber()) + ")");
			frame.getContentPane().setLayout(new BorderLayout());
			frame.getContentPane().add("Center", vis_panel);
			frame.pack();
			frame.setVisible(true);
		}
		
		pr.setVisible(false);
		
	}
	
	
	public void process() throws SPECCHIOClientException
	{
		progressBar.setString(null);
		
		// do the transformation, then call the module following the output space to do it's processing
		transform();
		
		set_progress(0);
		this.progressBar.setString("Waiting for data.");
		
	}
	
	public void configure() {
		
		// bring up the config window
		DialogThread dt = new DialogThread(new VisualisationSelectionDialog(get_coords_for_popups()), this, 1);
		dt.start();
	}

	public void user_data_provided(DialogThread dt) throws SPECCHIOClientException {
		boolean ret_val = dt.ret_val;
		
		super.user_data_provided(dt);
		
		if(ret_val == true)
		{
			if(dt.callback_value == 1)
			{
				vis_module_type = ((VisualisationSelectionDialog)dt.md).get_vis_module_type();	
				
				//this.module_name_label.setText(vis_module_type);
				
			}
		}
		
		processing_plane.redraw_object(ppo);
		
	}
	
	public void set_vis_module_type(String type)
	{
		this.vis_module_type = type;	
	}
	
	public int get_output_space_type() {
		return SpaceTypes.SpectralSpace;
	}


	public int get_required_input_space_type() {
		return SpaceTypes.SpectralSpace;
	}


}