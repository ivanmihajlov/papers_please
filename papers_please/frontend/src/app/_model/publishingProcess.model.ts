export interface PublishingProcess {
    processId: string;
    latestPaperId: string;
    latestLetterId: string;
    finishedReviewsIds: string[];
    paperTitles: string[];
    authors: string[];
    editorUsername: string;
    editorName: string;
    reviewers: string[];
    reviewersIds: string[];
    status: string;
    version: string;
}
