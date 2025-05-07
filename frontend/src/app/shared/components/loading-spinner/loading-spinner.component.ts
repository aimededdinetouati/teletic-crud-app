import { Component, Input } from '@angular/core';
import {CommonModule, NgClass} from "@angular/common";

@Component({
    selector: 'app-loading-spinner',
    templateUrl: './loading-spinner.component.html',
    imports: [
        NgClass,
        CommonModule
    ],
    styleUrls: ['./loading-spinner.component.css']
})
export class LoadingSpinnerComponent {
    @Input() size: 'small' | 'medium' | 'large' = 'medium';
    @Input() message: string = 'Loading...';
    @Input() overlay: boolean = false;
}