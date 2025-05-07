import { Component, Input, OnInit } from '@angular/core';
import {CommonModule} from "@angular/common";

export type AlertType = 'success' | 'info' | 'warning' | 'danger';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html',
    styleUrls: ['./alert.component.css'],
    imports: [
        CommonModule
    ]
})
export class AlertComponent implements OnInit {
    @Input() type: AlertType = 'info';
    @Input() message: string = '';
    @Input() dismissible: boolean = true;
    @Input() icon: boolean = true;
    @Input() autoClose: number = 0; // Time in milliseconds, 0 means no auto-close

    visible: boolean = true;
    iconClass: string = '';

    ngOnInit(): void {
        this.setIconClass();

        if (this.autoClose > 0) {
            setTimeout(() => this.close(), this.autoClose);
        }
    }

    setIconClass(): void {
        switch (this.type) {
            case 'success':
                this.iconClass = 'fa-check-circle';
                break;
            case 'warning':
                this.iconClass = 'fa-exclamation-triangle';
                break;
            case 'danger':
                this.iconClass = 'fa-times-circle';
                break;
            case 'info':
            default:
                this.iconClass = 'fa-info-circle';
                break;
        }
    }

    close(): void {
        this.visible = false;
    }
}