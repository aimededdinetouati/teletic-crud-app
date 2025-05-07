import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CommonModule, NgClass} from "@angular/common";

@Component({
    selector: 'app-confirm-dialog',
    templateUrl: './confirm-dialog.component.html',
    imports: [
        NgClass,
        CommonModule
    ],
    styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
    @Input() title: string = 'Confirm Action';
    @Input() message: string = 'Are you sure you want to proceed?';
    @Input() confirmText: string = 'Confirm';
    @Input() cancelText: string = 'Cancel';
    @Input() confirmButtonClass: string = 'btn-danger';
    @Input() visible: boolean = false;

    @Output() confirm = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

    onConfirm(): void {
        this.confirm.emit();
        this.visible = false;
    }

    onCancel(): void {
        this.cancel.emit();
        this.visible = false;
    }
}