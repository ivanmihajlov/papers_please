import { Component, OnInit } from '@angular/core';
import { PublishingProcess } from '../_model/publishingProcess.model';
import { PublishingProcessService } from '../_service/publishing-process.service';

@Component({
  selector: 'app-editor-page',
  templateUrl: './editor-page.component.html',
  styleUrls: ['./editor-page.component.scss']
})
export class EditorPageComponent implements OnInit {

  ongoingPublishingProcesses: PublishingProcess[];

  constructor(private publishingProcessService: PublishingProcessService) { }

  ngOnInit() {
    this.getOngoingPublishingProcess();
  }

  getOngoingPublishingProcess() {
    this.ongoingPublishingProcesses = [];
    this.publishingProcessService.getOngoingPublishingProcesses().subscribe(
      (response) => {
        this.ongoingPublishingProcesses = response;
      }
    );
  }

}
