namespace DGSTestInterface
{
	partial class Main
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.IContainer components = null;

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if(disposing && (components != null)) {
				components.Dispose();
			}
			base.Dispose(disposing);
		}

		#region Windows Form Designer generated code

		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.gb_ImageFiles = new System.Windows.Forms.GroupBox();
			this.btn_OutputFile = new System.Windows.Forms.Button();
			this.tb_OutputFile = new System.Windows.Forms.TextBox();
			this.label_OutputFile = new System.Windows.Forms.Label();
			this.btn_InputFile = new System.Windows.Forms.Button();
			this.tb_InputFile = new System.Windows.Forms.TextBox();
			this.label_InputFile = new System.Windows.Forms.Label();
			this.gb_Options = new System.Windows.Forms.GroupBox();
			this.btn_ProcessImages = new System.Windows.Forms.Button();
			this.gb_ProcessingLog = new System.Windows.Forms.GroupBox();
			this.tb_ProcessingLog = new System.Windows.Forms.TextBox();
			this.cb_ImageFormat = new System.Windows.Forms.ComboBox();
			this.gb_ImageFiles.SuspendLayout();
			this.gb_Options.SuspendLayout();
			this.gb_ProcessingLog.SuspendLayout();
			this.SuspendLayout();
			// 
			// gb_ImageFiles
			// 
			this.gb_ImageFiles.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.gb_ImageFiles.Controls.Add(this.btn_OutputFile);
			this.gb_ImageFiles.Controls.Add(this.tb_OutputFile);
			this.gb_ImageFiles.Controls.Add(this.label_OutputFile);
			this.gb_ImageFiles.Controls.Add(this.btn_InputFile);
			this.gb_ImageFiles.Controls.Add(this.tb_InputFile);
			this.gb_ImageFiles.Controls.Add(this.label_InputFile);
			this.gb_ImageFiles.Location = new System.Drawing.Point(12, 12);
			this.gb_ImageFiles.Name = "gb_ImageFiles";
			this.gb_ImageFiles.Size = new System.Drawing.Size(705, 70);
			this.gb_ImageFiles.TabIndex = 0;
			this.gb_ImageFiles.TabStop = false;
			this.gb_ImageFiles.Text = "Image Files";
			// 
			// btn_OutputFile
			// 
			this.btn_OutputFile.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btn_OutputFile.Location = new System.Drawing.Point(675, 37);
			this.btn_OutputFile.Name = "btn_OutputFile";
			this.btn_OutputFile.Size = new System.Drawing.Size(24, 23);
			this.btn_OutputFile.TabIndex = 5;
			this.btn_OutputFile.Text = "...";
			this.btn_OutputFile.UseVisualStyleBackColor = true;
			// 
			// tb_OutputFile
			// 
			this.tb_OutputFile.Location = new System.Drawing.Point(73, 39);
			this.tb_OutputFile.Name = "tb_OutputFile";
			this.tb_OutputFile.Size = new System.Drawing.Size(376, 20);
			this.tb_OutputFile.TabIndex = 4;
			this.tb_OutputFile.Text = "../../../../test.png";
			// 
			// label_OutputFile
			// 
			this.label_OutputFile.AutoSize = true;
			this.label_OutputFile.Location = new System.Drawing.Point(6, 42);
			this.label_OutputFile.Name = "label_OutputFile";
			this.label_OutputFile.Size = new System.Drawing.Size(61, 13);
			this.label_OutputFile.TabIndex = 3;
			this.label_OutputFile.Text = "Output File:";
			this.label_OutputFile.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// btn_InputFile
			// 
			this.btn_InputFile.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
			this.btn_InputFile.Location = new System.Drawing.Point(675, 11);
			this.btn_InputFile.Name = "btn_InputFile";
			this.btn_InputFile.Size = new System.Drawing.Size(24, 23);
			this.btn_InputFile.TabIndex = 2;
			this.btn_InputFile.Text = "...";
			this.btn_InputFile.UseVisualStyleBackColor = true;
			this.btn_InputFile.Click += new System.EventHandler(this.btn_InputFile_Click);
			// 
			// tb_InputFile
			// 
			this.tb_InputFile.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.tb_InputFile.Location = new System.Drawing.Point(73, 13);
			this.tb_InputFile.Name = "tb_InputFile";
			this.tb_InputFile.Size = new System.Drawing.Size(596, 20);
			this.tb_InputFile.TabIndex = 1;
			this.tb_InputFile.Text = "../../../../examples/substituteVariables.svg";
			// 
			// label_InputFile
			// 
			this.label_InputFile.AutoSize = true;
			this.label_InputFile.Location = new System.Drawing.Point(14, 16);
			this.label_InputFile.Name = "label_InputFile";
			this.label_InputFile.Size = new System.Drawing.Size(53, 13);
			this.label_InputFile.TabIndex = 0;
			this.label_InputFile.Text = "Input File:";
			this.label_InputFile.TextAlign = System.Drawing.ContentAlignment.MiddleRight;
			// 
			// gb_Options
			// 
			this.gb_Options.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.gb_Options.Controls.Add(this.cb_ImageFormat);
			this.gb_Options.Location = new System.Drawing.Point(12, 88);
			this.gb_Options.Name = "gb_Options";
			this.gb_Options.Size = new System.Drawing.Size(705, 93);
			this.gb_Options.TabIndex = 1;
			this.gb_Options.TabStop = false;
			this.gb_Options.Text = "Processing Options";
			// 
			// btn_ProcessImages
			// 
			this.btn_ProcessImages.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
			this.btn_ProcessImages.Location = new System.Drawing.Point(642, 350);
			this.btn_ProcessImages.Name = "btn_ProcessImages";
			this.btn_ProcessImages.Size = new System.Drawing.Size(75, 23);
			this.btn_ProcessImages.TabIndex = 2;
			this.btn_ProcessImages.Text = "Go";
			this.btn_ProcessImages.UseVisualStyleBackColor = true;
			this.btn_ProcessImages.Click += new System.EventHandler(this.btn_ProcessImages_Click);
			// 
			// gb_ProcessingLog
			// 
			this.gb_ProcessingLog.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
						| System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.gb_ProcessingLog.Controls.Add(this.tb_ProcessingLog);
			this.gb_ProcessingLog.Location = new System.Drawing.Point(12, 187);
			this.gb_ProcessingLog.Name = "gb_ProcessingLog";
			this.gb_ProcessingLog.Size = new System.Drawing.Size(705, 157);
			this.gb_ProcessingLog.TabIndex = 3;
			this.gb_ProcessingLog.TabStop = false;
			this.gb_ProcessingLog.Text = "Processing Log";
			// 
			// tb_ProcessingLog
			// 
			this.tb_ProcessingLog.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
						| System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.tb_ProcessingLog.Location = new System.Drawing.Point(6, 19);
			this.tb_ProcessingLog.Multiline = true;
			this.tb_ProcessingLog.Name = "tb_ProcessingLog";
			this.tb_ProcessingLog.Size = new System.Drawing.Size(693, 132);
			this.tb_ProcessingLog.TabIndex = 0;
			// 
			// cb_ImageFormat
			// 
			this.cb_ImageFormat.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
			this.cb_ImageFormat.FormattingEnabled = true;
			this.cb_ImageFormat.Items.AddRange(new object[] {
            "PNG",
            "JPG",
            "TIFF",
            "PDF"});
			this.cb_ImageFormat.Location = new System.Drawing.Point(6, 19);
			this.cb_ImageFormat.Name = "cb_ImageFormat";
			this.cb_ImageFormat.Size = new System.Drawing.Size(189, 21);
			this.cb_ImageFormat.TabIndex = 0;
			this.cb_ImageFormat.SelectedIndexChanged += new System.EventHandler(this.cb_ImageFormat_SelectedIndexChanged);
			// 
			// Main
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(729, 385);
			this.Controls.Add(this.gb_ProcessingLog);
			this.Controls.Add(this.btn_ProcessImages);
			this.Controls.Add(this.gb_Options);
			this.Controls.Add(this.gb_ImageFiles);
			this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
			this.Name = "Main";
			this.Text = "Digital Graphics Server Test Client";
			this.gb_ImageFiles.ResumeLayout(false);
			this.gb_ImageFiles.PerformLayout();
			this.gb_Options.ResumeLayout(false);
			this.gb_ProcessingLog.ResumeLayout(false);
			this.gb_ProcessingLog.PerformLayout();
			this.ResumeLayout(false);

		}

		#endregion

		private System.Windows.Forms.GroupBox gb_ImageFiles;
		private System.Windows.Forms.Button btn_InputFile;
		private System.Windows.Forms.TextBox tb_InputFile;
		private System.Windows.Forms.Label label_InputFile;
		private System.Windows.Forms.Button btn_OutputFile;
		private System.Windows.Forms.TextBox tb_OutputFile;
		private System.Windows.Forms.Label label_OutputFile;
		private System.Windows.Forms.GroupBox gb_Options;
		private System.Windows.Forms.Button btn_ProcessImages;
		private System.Windows.Forms.GroupBox gb_ProcessingLog;
		private System.Windows.Forms.TextBox tb_ProcessingLog;
		private System.Windows.Forms.ComboBox cb_ImageFormat;

	}
}

